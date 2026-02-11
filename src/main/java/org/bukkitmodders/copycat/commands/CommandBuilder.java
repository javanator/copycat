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
import org.bukkit.entity.Player;
import org.bukkitmodders.copycat.Application;
import org.bukkitmodders.copycat.managers.PlayerSettingsManager;
import org.bukkitmodders.copycat.model.BuildContext;
import org.bukkitmodders.copycat.model.BuildContextFactory;
import org.bukkitmodders.copycat.model.PlayerSettingsType;
import org.bukkitmodders.copycat.services.MediaService;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;


public class CommandBuilder {

    private final Application application;
    private final MediaService mediaService;

    public CommandBuilder(Application application, MediaService mediaService) {
        this.application = application;
        this.mediaService = mediaService;
    }

    private CompletableFuture<Suggestions> buildShortcutSuggestions(CommandContext<CommandSourceStack> commandContext, SuggestionsBuilder suggestionsBuilder) {
        String playerName = commandContext.getSource().getSender().getName();
        PlayerSettingsManager playerSettings = application.getPlayerSettings(playerName);

        String input = suggestionsBuilder.getRemaining().toLowerCase();

        if (playerSettings.getShortcuts() != null) {
            playerSettings.getShortcuts().stream()
                    .map(PlayerSettingsType.Shortcut::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
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
                    String playerName = context.getSource().getSender().getName();
                    PlayerSettingsManager playerSettings = application.getPlayerSettings(playerName);
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
                    String name = commandContext.getArgument("name", String.class);
                    String playerName = commandContext.getSource().getSender().getName();
                    PlayerSettingsManager playerSettings = application.getPlayerSettings(playerName);
                    playerSettings.deleteShortcut(name);
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
                                BuildContext buildContext = BuildContextFactory.create(application, player, foundShortcut);
                                application.getServer().getAsyncScheduler().runNow(application, (scheduledTask) -> {
                                    mediaService.executeHttpRequest(player, foundShortcut, buildContext);
                                });
                            }

                            return Command.SINGLE_SUCCESS;
                        }));
    }

    public LiteralArgumentBuilder<CommandSourceStack> buildUndoCommand() {
        return Commands.literal("undo")
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getSender();
                    application.getPlayerSettings(player.getName()).undo(player);
                    return Command.SINGLE_SUCCESS;
                });
    }

    public LiteralArgumentBuilder<CommandSourceStack> buildStopCommand() {
        return Commands.literal("stop")
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getSender();
                    mediaService.stopVideoStreamsForPlayer(player);
                    return Command.SINGLE_SUCCESS;
                });
    }

    public LiteralArgumentBuilder<CommandSourceStack> buildSetCommand() {
        return Commands.literal("set")
                .then(Commands.literal("dithering").then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(context -> {
                            boolean ditheringEnabled = context.getArgument("enabled", Boolean.class);
                            String playerName = context.getSource().getSender().getName();
                            PlayerSettingsManager playerSettings = application.getPlayerSettings(playerName);
                            playerSettings.setDithering(ditheringEnabled);
                            return Command.SINGLE_SUCCESS;
                        })))
                .then(Commands.literal("dimensions")
                        .then(Commands.argument("width", IntegerArgumentType.integer())
                                .then(Commands.argument("height", IntegerArgumentType.integer())
                                        .executes(ctx -> {
                                            Integer width = ctx.getArgument("width", Integer.class);
                                            Integer height = ctx.getArgument("height", Integer.class);
                                            String playerName = ctx.getSource().getSender().getName();
                                            PlayerSettingsManager playerSettings = application.getPlayerSettings(playerName);
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
