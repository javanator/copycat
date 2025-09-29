package org.bukkitmodders.copycat.commands;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import lombok.SneakyThrows;
import okhttp3.*;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkitmodders.copycat.Application;
import org.bukkitmodders.copycat.managers.PlayerSettingsManager;
import org.bukkitmodders.copycat.model.PlayerSettingsType;
import org.bukkitmodders.copycat.model.RevertibleBlock;
import org.bukkitmodders.copycat.services.Canvas;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingDeque;


public class CommandBuilder {

    private final OkHttpClient httpClient = new OkHttpClient.Builder().followRedirects(true).build();
    private final Application application;

    public CommandBuilder(Application application) {
        this.application = application;
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
                    PlayerSettingsManager playerSettings = application.getConfigurationManager().getPlayerSettings(player);
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
                    PlayerSettingsManager playerSettings = application.getConfigurationManager().getPlayerSettings(playerName);
                    playerSettings.addShortcut(name, url);
                    return Command.SINGLE_SUCCESS;
                });
    }

    public LiteralArgumentBuilder<CommandSourceStack> buildRemoveCommand() {
        return Commands.literal("remove")
                .then(Commands.argument("name", StringArgumentType.string())
                        .suggests((commandContext, suggestionsBuilder) -> {
                            String player = commandContext.getSource().getSender().getName();
                            PlayerSettingsManager playerSettings = application.getConfigurationManager().getPlayerSettings(player);
                            // Get the partial input to filter suggestions
                            String input = suggestionsBuilder.getInput();
                            String[] parts = input.split(" ");
                            String partial = parts.length > 0 ? parts[parts.length - 1] : "";
                            // Add matching shortcuts as suggestions
                            if (playerSettings.getShortcuts() != null) {
                                playerSettings.getShortcuts().stream()
                                        .map(shortcut -> shortcut.getName())
                                        .filter(name -> name.toLowerCase().startsWith(partial.toLowerCase()))
                                        .forEach(name -> suggestionsBuilder.suggest(name));
                            }
                            return suggestionsBuilder.buildFuture();
                        }))
                .executes(commandContext -> {
                    return Command.SINGLE_SUCCESS;
                });
    }

    public LiteralArgumentBuilder<CommandSourceStack> buildCopyCommand() {
        return Commands.literal("copy")
                .then(Commands.argument("shortcut", StringArgumentType.string())
                        .suggests((commandContext, suggestionsBuilder) -> {
                            String player = commandContext.getSource().getSender().getName();
                            PlayerSettingsManager playerSettings = application.getConfigurationManager().getPlayerSettings(player);
                            // Get the partial input to filter suggestions
                            String input = suggestionsBuilder.getInput();
                            String[] parts = input.split(" ");
                            String partial = parts.length > 0 ? parts[parts.length - 1] : "";
                            // Add matching shortcuts as suggestions
                            if (playerSettings.getShortcuts() != null) {
                                playerSettings.getShortcuts().stream()
                                        .map(shortcut -> shortcut.getName())
                                        .filter(name -> name.toLowerCase().startsWith(partial.toLowerCase()))
                                        .forEach(name -> suggestionsBuilder.suggest(name));
                            }
                            return suggestionsBuilder.buildFuture();
                        })
                        .executes(context -> {
                            String shortcutName = context.getArgument("shortcut", String.class);
                            Player player = (Player) context.getSource().getSender();
                            PlayerSettingsManager playerSettings = application.getConfigurationManager().getPlayerSettings(player.getName());
                            PlayerSettingsType.Shortcut foundShortcut = playerSettings.getShortcuts().stream()
                                    .filter(s -> s.getName().equalsIgnoreCase(shortcutName))
                                    .findFirst()
                                    .orElse(null);
                            if (foundShortcut != null) {
                                player.sendMessage("URL for " + shortcutName + ": " + foundShortcut.getUrl());
                                Request request = new Request.Builder().url(foundShortcut.getUrl()).build();
                                httpClient.newCall(request).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                        //Fail
                                        player.sendMessage("Failed to download " + foundShortcut.getUrl());
                                    }

                                    @SneakyThrows
                                    @Override
                                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                        //Render
                                        BufferedImage image = ImageIO.read(response.body().byteStream());

                                        Block b = player.getTargetBlock(null, 100);
                                        Location location = new Location(b.getWorld(), b.getX(), b.getY(), b.getZ());
                                        location.setYaw(player.getLocation().getYaw());
                                        location.setPitch(player.getLocation().getPitch());

                                        Canvas canvas = new Canvas(image, player, location);

                                        BukkitScheduler scheduler = application.getServer().getScheduler();
                                        scheduler.runTask(application, new Runnable() {
                                            @Override
                                            public void run() {
                                                canvas.performDraw();
                                            }
                                        });
                                    }
                                });
                            }
                            return Command.SINGLE_SUCCESS;
                        }));
    }

    public LiteralArgumentBuilder<CommandSourceStack> buildUndoCommand() {
        return Commands.literal("undo")
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getSender();
                    ctx.getSource().getSender().sendMessage("Undo for " + player.getName());

                    PlayerSettingsManager playerSettings = application.getConfigurationManager().getPlayerSettings(player.getName());
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
                .then(Commands.literal("poll").executes(context -> {
                    return Command.SINGLE_SUCCESS;
                }));
    }

    public LiteralArgumentBuilder<CommandSourceStack> buildSetCommand() {
        return Commands.literal("set")
                .then(Commands.literal("dithering").then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(context -> {
                            boolean ditheringEnabled = context.getArgument("enabled", Boolean.class);
                            String player = context.getSource().getSender().getName();
                            PlayerSettingsManager playerSettings = application.getConfigurationManager().getPlayerSettings(player);
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
                                            PlayerSettingsManager playerSettings = application.getConfigurationManager().getPlayerSettings(player);
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
