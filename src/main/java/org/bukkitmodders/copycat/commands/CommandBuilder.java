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
<<<<<<< HEAD
import org.bukkitmodders.copycat.services.MediaService;

=======
import org.bukkitmodders.copycat.model.RevertibleBlock;
import org.bukkitmodders.copycat.model.PolledSourceType;
import org.bukkitmodders.copycat.services.PrepareImageTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.time.Duration;
>>>>>>> 7ef0926 (checkpoint)
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

<<<<<<< HEAD
=======
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
                    try {
                        // Create a PolledSourceType and save it using PlayerSettingsManager
                        PolledSourceType source = new PolledSourceType();
                        source.setShortcutNameRef(shortcut.getName());
                        // Use player's current target location and orientation
                        source.setWorldX((long) location.getX());
                        source.setWorldY((long) location.getY());
                        source.setYaw(location.getYaw());
                        // Default refresh rate; could be sourced from global settings if exposed
                        source.setRefreshRateMilliseconds(200);

                        PlayerSettingsManager psm = application.getPlayerSettings(player.getName());
                        psm.addPolledSource(source);
                        player.sendMessage("Started polling for shortcut: " + shortcut.getName());
                    } catch (Exception ex) {
                        LOG.error("Failed to add polled source for shortcut: " + shortcut.getName(), ex);
                        player.sendMessage("Failed to start polling for " + shortcut.getName());
                    }
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
            PrepareImageTask canvas = new PrepareImageTask(application, buildContext);

            //Get back on the main thread for render
            BukkitScheduler scheduler = application.getServer().getScheduler();
            scheduler.runTask(application, canvas::performDraw);
        } catch (Throwable e) {
            LOG.error("Failed to process image from URL: ", e);
        }
    }

    void handleVideoStreamRequest(Player player, String streamUrl) {
        BukkitScheduler scheduler = application.getServer().getScheduler();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(streamUrl))
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            HttpClient httpClient = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(30))
                    .build();

            CompletableFuture<HttpResponse<java.io.InputStream>> responseFuture =
                    httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream());

            responseFuture.whenComplete((response, throwable) -> {
                if (throwable != null) {
                    LOG.error("Error fetching video stream: " + streamUrl, throwable);
                    player.sendMessage("Failed to fetch video stream");
                    return;
                }

                try {
                    // Get player settings for dimensions
                    PlayerSettingsManager playerSettings = application.getPlayerSettings(player.getName());
                    int width = playerSettings.getMaxBuildWidth();
                    int height = playerSettings.getMaxBuildHeight();

                    // Initialize VLC4J media player factory
                    uk.co.caprica.vlcj.factory.MediaPlayerFactory factory = new uk.co.caprica.vlcj.factory.MediaPlayerFactory();

                    // Create a callback media player with custom rendering
                    uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer mediaPlayer = factory.mediaPlayers().newEmbeddedMediaPlayer();

                    // Set up the video surface with a callback for custom rendering
                    mediaPlayer.videoSurface().set(factory.videoSurfaces().newVideoSurface(
                            new uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback() {
                                @Override
                                public uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
                                    return new uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat(width, height);
                                }

                                @Override
                                public void newFormatSize(int i, int i1, int i2, int i3) {

                                }

                                @Override
                                public void allocatedBuffers(ByteBuffer[] byteBuffers) {

                                }
                            },
                            new uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback() {
                                @Override
                                public void lock(uk.co.caprica.vlcj.player.base.MediaPlayer mediaPlayer) {

                                }

                                @Override
                                public void display(uk.co.caprica.vlcj.player.base.MediaPlayer mediaPlayer, java.nio.ByteBuffer[] nativeBuffers, uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat bufferFormat, int x, int y) {
                                    // Convert ByteBuffer to BufferedImage
                                    int bufferWidth = bufferFormat.getWidth();
                                    int bufferHeight = bufferFormat.getHeight();
                                    BufferedImage bufferedImage = new BufferedImage(bufferWidth, bufferHeight, BufferedImage.TYPE_INT_RGB);
                                    int[] pixels = new int[bufferWidth * bufferHeight];
                                    nativeBuffers[0].asIntBuffer().get(pixels);
                                    bufferedImage.setRGB(0, 0, bufferWidth, bufferHeight, pixels, 0, bufferWidth);

                                    // Get build context
                                    Block b = player.getTargetBlock(null, 100);
                                    Location location = new Location(b.getWorld(), b.getX(), b.getY(), b.getZ());
                                    location.setYaw(player.getLocation().getYaw());
                                    location.setPitch(player.getLocation().getPitch());

                                    String blockProfileName = playerSettings.getBlockProfile();
                                    BlockProfileType blockProfile = application.getConfigurationManager().getBlockProfile(blockProfileName);

                                    BuildContext buildContext = BuildContext.builder()
                                            .withPlayer(player)
                                            .withLocation(location)
                                            .withBlockProfile(blockProfile)
                                            .withImage(bufferedImage)
                                            .build();

                                    PrepareImageTask task = new PrepareImageTask(application, buildContext);
                                    scheduler.runTask(application, task::performDraw);
                                }

                                @Override
                                public void unlock(uk.co.caprica.vlcj.player.base.MediaPlayer mediaPlayer) {

                                }
                            },
                            true
                    ));

                    // Play the stream URL
                    mediaPlayer.media().play(streamUrl);

                    player.sendMessage("Video stream opened with VLC4J custom canvas: " + streamUrl);
                    LOG.info("Opened video stream for player {} with canvas {}x{}: {}",
                            player.getName(), width, height, streamUrl);

                } catch (Exception e) {
                    LOG.error("Failed to open video stream with VLC4J", e);
                    player.sendMessage("Failed to open video stream with VLC4J");
                }
            });

        } catch (Exception e) {
            player.sendMessage("Failed to create video stream request for " + streamUrl);
            LOG.error("Failed to create video stream request for " + streamUrl, e);
        }
    }

>>>>>>> 7ef0926 (checkpoint)
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
