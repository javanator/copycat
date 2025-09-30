package org.bukkitmodders.copycat.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.util.Tick;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkitmodders.copycat.Application;
import org.bukkitmodders.copycat.managers.PlayerSettingsManager;
import org.bukkitmodders.copycat.model.BlockProfileType;
import org.bukkitmodders.copycat.model.BuildContext;
import org.bukkitmodders.copycat.model.PlayerSettingsType;
import org.bukkitmodders.copycat.model.RevertibleBlock;
import org.bukkitmodders.copycat.services.CopyTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingDeque;


public class CommandBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(CommandBuilder.class);
    private final Application application;

    public CommandBuilder(Application application) {
        this.application = application;
    }

    private CompletableFuture<Suggestions> buildShortcutSuggestions(CommandContext<CommandSourceStack> commandContext, SuggestionsBuilder suggestionsBuilder) {
        String player = commandContext.getSource().getSender().getName();
        PlayerSettingsManager playerSettings = application.getPlayerSettings(player);

        // Get the partial input to filter suggestions
        String input = suggestionsBuilder.getInput();
        String[] parts = input.split(" ");
        String partial = parts.length > 0 ? parts[parts.length - 1] : "";

        // Add matching shortcuts as suggestions
        if (playerSettings.getShortcuts() != null) {
            playerSettings.getShortcuts().stream()
                    .map(PlayerSettingsType.Shortcut::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial.toLowerCase()))
                    .forEach(suggestionsBuilder::suggest);
        }
        return suggestionsBuilder.buildFuture();
    }

    private PlayerSettingsType.Shortcut findShortcut(String shortcutName, PlayerSettingsManager playerSettings) {
        return playerSettings.getShortcuts().stream()
                .filter(s -> s.getName().equalsIgnoreCase(shortcutName))
                .findFirst()
                .orElse(null);
    }

    private void executeHttpRequest(Player player, PlayerSettingsType.Shortcut shortcut, boolean isPolling) {
        BukkitScheduler scheduler = application.getServer().getScheduler();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(shortcut.getUrl()))
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            HttpClient httpClient = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(30))
                    .authenticator(PlayerAuthenticator.builder()
                            .application(application)
                            .player(player)
                            .build())
                    .build();

            //Begin create build contesxt
            Block b = player.getTargetBlock(null, 100);
            Location location = new Location(b.getWorld(), b.getX(), b.getY(), b.getZ());
            location.setYaw(player.getLocation().getYaw());
            location.setPitch(player.getLocation().getPitch());

            String blockProfileName = application.getPlayerSettings(player.getName()).getBlockProfile();
            BlockProfileType blockProfile = application.getConfigurationManager().getBlockProfile(blockProfileName);

            BuildContext buildContext = BuildContext.builder()
                    .withPlayer(player)
                    .withLocation(location)
                    .withBlockProfile(blockProfile)
                    .withShortcut(shortcut)
                    .build();
            //end create build context

            CompletableFuture<HttpResponse<byte[]>> responseFuture = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray());
            responseFuture.whenComplete((response, throwable) -> {
                if (throwable != null) {
                    LOG.error("Error downloading: " + response.request().uri().toString(), throwable);
                }

                if (isPolling) {
                    //TODO: Fire and forget! Task should be tracked in order to start/stop/cancel.
                    //TODO: store and read polling interval from plugin config
                    Runnable refresh = () -> handleHttpResponse(response, buildContext);
                    //Period is in server ticks (50ms default)
                    int ticks = Tick.tick().fromDuration(Duration.ofMillis(200));
                    scheduler.scheduleSyncRepeatingTask(application, refresh, 0, ticks);
                } else {
                    handleHttpResponse(response, buildContext);
                }
            });
        } catch (Exception e) {
            player.sendMessage("Failed to create request for " + shortcut.getUrl());
            LOG.error("Failed to create HTTP request for " + shortcut.getUrl(), e);
        }
    }

    void handleHttpResponse(HttpResponse<byte[]> response, BuildContext buildContext) {
        try {
            // Add the image to build context. Find a better way to stop mutating state later.
            BufferedImage image = ImageIO.read(new java.io.ByteArrayInputStream(response.body()));
            buildContext.setImage(image);
            CopyTask canvas = new CopyTask(application, buildContext);

            //Get back on the main thread for render
            BukkitScheduler scheduler = application.getServer().getScheduler();
            scheduler.runTask(application, canvas::performDraw);
        } catch (Throwable e) {
            LOG.error("Failed to process image from URL: ", e);
        }
    }

    public LiteralArgumentBuilder<CommandSourceStack> buildAdminCommand() {
        return Commands.literal("admin")
                .then(Commands.literal("undo")
                        .then(Commands.argument("player", ArgumentTypes.player())
                                .executes(ctx -> {
                                    final PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
                                    final Player target = targetResolver.resolve(ctx.getSource()).getFirst();
                                    ctx.getSource().getSender().sendMessage("Undo on " + Objects.requireNonNull(target.getPlayer()).getName());
                                    return Command.SINGLE_SUCCESS;
                                })));
    }

    public LiteralArgumentBuilder<CommandSourceStack> buildListCommand() {
        return Commands.literal("list")
                .executes(context -> {
                    String player = context.getSource().getSender().getName();
                    PlayerSettingsManager playerSettings = application.getPlayerSettings(player);
                    context.getSource().getSender().sendMessage("Copycat Image List:");
                    playerSettings.getShortcuts().forEach(s -> {
                        context.getSource().getSender().sendMessage(s.getName() + " URL: " + s.getUrl());
                    });
                    return Command.SINGLE_SUCCESS;
                });
    }

    public LiteralArgumentBuilder<CommandSourceStack> buildAddCommand() {
        return Commands.literal("add")
                .then(Commands.argument("name", StringArgumentType.string()))
                .then(Commands.argument("url", StringArgumentType.string()))
                .executes(commandContext -> {
                    String name = commandContext.getArgument("name", String.class);
                    String url = commandContext.getArgument("url", String.class);
                    String playerName = commandContext.getSource().getSender().getName();
                    PlayerSettingsManager playerSettings = application.getPlayerSettings(playerName);
                    playerSettings.addShortcut(name, url);
                    return Command.SINGLE_SUCCESS;
                });
    }

    public LiteralArgumentBuilder<CommandSourceStack> buildRemoveCommand() {
        return Commands.literal("remove")
                .then(Commands.argument("name", StringArgumentType.string())
                        .suggests(this::buildShortcutSuggestions))
                .executes(commandContext -> {
                    return Command.SINGLE_SUCCESS;
                });
    }

    public LiteralArgumentBuilder<CommandSourceStack> buildCopyCommand() {
        return Commands.literal("copy")
                .then(Commands.argument("shortcut", StringArgumentType.string())
                        .suggests(this::buildShortcutSuggestions)
                        .executes(context -> {
                            String shortcutName = context.getArgument("shortcut", String.class);
                            Player player = (Player) context.getSource().getSender();
                            PlayerSettingsManager playerSettings = application.getPlayerSettings(player.getName());
                            PlayerSettingsType.Shortcut foundShortcut = findShortcut(shortcutName, playerSettings);

                            if (foundShortcut != null) {
                                player.sendMessage("URL for " + shortcutName + ": " + foundShortcut.getUrl());
                                executeHttpRequest(player, foundShortcut, false);
                            }
                            return Command.SINGLE_SUCCESS;
                        }));
    }

    public LiteralArgumentBuilder<CommandSourceStack> buildUndoCommand() {
        return Commands.literal("undo")
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getSender();
                    ctx.getSource().getSender().sendMessage("Undo for " + player.getName());

                    PlayerSettingsManager playerSettings = application.getPlayerSettings(player.getName());
                    LinkedBlockingDeque<Stack<RevertibleBlock>> undoBuffer = playerSettings.getUndoBuffer();

                    if (!undoBuffer.isEmpty()) {
                        Stack<RevertibleBlock> lastImageBlocks = undoBuffer.pop();

                        while (!lastImageBlocks.isEmpty()) {
                            lastImageBlocks.pop().revert();
                        }
                    }

                    return Command.SINGLE_SUCCESS;
                });
    }

    public LiteralArgumentBuilder<CommandSourceStack> buildPollCommand() {
        return Commands.literal("poll")
                .then(Commands.argument("shortcut", StringArgumentType.string())
                        .suggests(this::buildShortcutSuggestions)
                        .executes(context -> {
                            String shortcutName = context.getArgument("shortcut", String.class);
                            Player player = (Player) context.getSource().getSender();
                            PlayerSettingsManager playerSettings = application.getPlayerSettings(player.getName());
                            PlayerSettingsType.Shortcut foundShortcut = findShortcut(shortcutName, playerSettings);

                            if (foundShortcut != null) {
                                player.sendMessage("URL for " + shortcutName + ": " + foundShortcut.getUrl());
                                executeHttpRequest(player, foundShortcut, true);
                            }
                            return Command.SINGLE_SUCCESS;
                        }));
    }

    public LiteralArgumentBuilder<CommandSourceStack> buildSetCommand() {
        return Commands.literal("set")
                .then(Commands.literal("dithering").then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(context -> {
                            boolean ditheringEnabled = context.getArgument("enabled", Boolean.class);
                            String player = context.getSource().getSender().getName();
                            PlayerSettingsManager playerSettings = application.getPlayerSettings(player);
                            playerSettings.setDithering(ditheringEnabled);
                            return Command.SINGLE_SUCCESS;
                        })))
                .then(Commands.literal("dimensions")
                        .then(Commands.argument("width", IntegerArgumentType.integer())
                                .then(Commands.argument("height", IntegerArgumentType.integer())
                                        .executes(ctx -> {
                                            Integer width = ctx.getArgument("width", Integer.class);
                                            Integer height = ctx.getArgument("height", Integer.class);
                                            String player = ctx.getSource().getSender().getName();
                                            PlayerSettingsManager playerSettings = application.getPlayerSettings(player);
                                            playerSettings.setBuildDimensions(width, height);
                                            return Command.SINGLE_SUCCESS;
                                        }))))
                .then(Commands.literal("profile")
                        .then(Commands.argument("profileName", StringArgumentType.string())
                                .suggests((commandContext, suggestionsBuilder) -> {
                                    // Get available block profiles for tab completion
                                    return suggestionsBuilder.buildFuture();
                                })));
    }
}
