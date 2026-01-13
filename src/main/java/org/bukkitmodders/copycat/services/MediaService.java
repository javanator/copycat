package org.bukkitmodders.copycat.services;

import org.apache.commons.io.IOUtils;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkitmodders.copycat.Application;
import org.bukkitmodders.copycat.commands.PlayerAuthenticator;
import org.bukkitmodders.copycat.managers.PlayerSettingsManager;
import org.bukkitmodders.copycat.model.BuildContext;
import org.bukkitmodders.copycat.model.PlayerSettingsType;
import org.bukkitmodders.copycat.model.RevertibleBlock;
import org.bukkitmodders.copycat.model.UndoHistoryComponent;
import org.bukkitmodders.copycat.util.ImageUtil;
import org.bukkitmodders.copycat.util.MatrixUtil;
import org.bukkitmodders.copycat.util.TimeoutInputStream;
import org.joml.Matrix4d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallbackAdapter;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;
import uk.co.caprica.vlcj.player.media.callback.nonseekable.NonSeekableInputStreamMedia;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.IndexColorModel;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class MediaService {
    private static final Logger LOG = LoggerFactory.getLogger(MediaService.class);
    private final Application application;
    private final Map<String, Collection<DirectMediaPlayer>> activeMediaPlayers = new ConcurrentHashMap<>();
    private final MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory();

    public MediaService(Application application) {
        this.application = application;
    }

    public void executeHttpRequest(Player player, PlayerSettingsType.Shortcut shortcut, BuildContext buildContext) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(shortcut.getUrl()))
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            // Create a specialized client if authentication is needed, or just use a specialized authenticator per request
            // The original code used a new HttpClient per request to set the authenticator.
            HttpClient requestClient = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(30))
                    .authenticator(PlayerAuthenticator.builder()
                            .application(application)
                            .player(player)
                            .build())
                    .build();

            CompletableFuture<HttpResponse<InputStream>> responseFuture = requestClient.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream());
            responseFuture.whenComplete((response, throwable) -> {
                if (throwable != null) {
                    LOG.error("Error downloading: {}", request.uri().toString(), throwable);
                    return;
                }

                handleHttpResponse(player, shortcut, response, buildContext);
            });
        } catch (Exception e) {
            player.sendMessage("Failed to create request for " + shortcut.getUrl());
            LOG.error("Failed to create HTTP request for {}", shortcut.getUrl(), e);
        }
    }

    private void handleHttpResponse(Player player, PlayerSettingsType.Shortcut shortcut, HttpResponse<InputStream> response, BuildContext buildContext) {
        try {
            String mimeType = response.headers().firstValue("Content-Type").orElse("application/octet-stream");

            boolean isVideo = mimeType.startsWith("video/");
            boolean isImage = mimeType.startsWith("image/");
            boolean isMultipart = mimeType.startsWith("multipart/"); //multipart/x-mixed-replace;boundary=boundarySample

            if (isImage) {
                BufferedImage image = ImageIO.read(new java.io.ByteArrayInputStream(IOUtils.toByteArray(response.body())));
                buildContext.setImage(image);
                PrepareImageTask canvas = new PrepareImageTask(application, buildContext);
                BukkitScheduler scheduler = application.getServer().getScheduler();
                scheduler.runTask(application, canvas::performDraw);
            } else if (isVideo) {
                handleVideoStreamRequest(player, buildContext, response);
            } else if (isMultipart) {
                //Assume video
                //For MJPEG Each multipart segment is represented as a single video frame
                //--boundarySample
                //Content-Type: image/jpeg
                //Content-Length: 46045

                handleVideoStreamRequest(player, buildContext, response);
            }
        } catch (Throwable e) {
            LOG.error("Failed to process image from URL: ", e);
        }
    }

    public void handleVideoStreamRequest(Player player, BuildContext buildContext, HttpResponse<InputStream> response) {
        try {
            PlayerSettingsManager playerSettings = application.getPlayerSettings(player.getName());
            int width = playerSettings.getMaxBuildWidth();
            int height = playerSettings.getMaxBuildHeight();

            TextureMapProcessor textureMapProcessor = new TextureMapProcessor(buildContext.getBlockProfile());
            Matrix4d rotationMatrix = MatrixUtil.calculateRotation(buildContext.getLocation());
            ImageCopier imageCopier = new ImageCopier(textureMapProcessor, buildContext.getLocation(), rotationMatrix);
            AtomicBoolean drawing = new AtomicBoolean(false);
            IndexColorModel icm = ImageUtil.generateIndexColorModel(textureMapProcessor.getColorTable().keySet());

            DirectMediaPlayer mediaPlayer = mediaPlayerFactory.newDirectMediaPlayer(
                    new BufferFormatCallback() {
                        @Override
                        public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
                            return new RV32BufferFormat(width, height);
                        }
                    },
                    new RenderCallbackAdapter(new int[width * height]) {
                        @Override
                        public void onDisplay(DirectMediaPlayer mediaPlayer, int[] data) {
                            if (drawing.get()) {
                                return;
                            }
                            drawing.set(true);

                            application.getServer().getScheduler().runTask(application, () -> {
                                try {
                                    BufferedImage frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                                    int[] frameData = ((DataBufferInt) frame.getRaster().getDataBuffer()).getData();
                                    System.arraycopy(data, 0, frameData, 0, data.length);

                                    if (playerSettings.isDithering()) {
                                        frame = ImageUtil.ditherImage(frame, icm);
                                    }

                                    imageCopier.draw(frame, null);
                                } catch (Exception e) {
                                    LOG.error("Error drawing video frame", e);
                                } finally {
                                    drawing.set(false);
                                }
                            });
                        }
                    }
            );

            activeMediaPlayers.computeIfAbsent(player.getName(), k -> new CopyOnWriteArrayList<>()).add(mediaPlayer);

            UndoHistoryComponent undoComponent = UndoHistoryComponent.builder()
                    .withBlocks(imageCopier.createUndoBuffer(width, height))
                    .withMediaPlayer(mediaPlayer)
                    .build();
            playerSettings.getUndoBuffer().push(undoComponent);

            mediaPlayer.playMedia(new NonSeekableInputStreamMedia() {
                @Override
                protected InputStream onOpenStream() {
                    // This is necessary because the native thread of VLCJ uses blocking reads to
                    // read the stream
                    return new TimeoutInputStream(response.body(), 10000); // 10-second read timeout
                }

                @Override
                protected void onCloseStream(InputStream inputStream) {
                    application.getLogger().info("onCloseStream()");
                    application.getServer().getScheduler().runTask(application, () -> {
                        undoComponent.getBlocks().forEach(RevertibleBlock::revert);
                    });
                }

                @Override
                protected long onGetSize() {
                    return -1;
                }
            });

            player.sendMessage("Video stream opened: " + buildContext.getShortcut().getUrl());
        } catch (Exception e) {
            LOG.error("Failed to open video stream with VLC4J: {}", buildContext.getShortcut().getUrl(), e);
            player.sendMessage("Failed to open video stream");
        }
    }

    public void stopVideoStreamsForPlayer(Player player) {
        Collection<DirectMediaPlayer> players = activeMediaPlayers.remove(player.getName());
        if (players != null) {
            for (DirectMediaPlayer mediaPlayer : players) {
                mediaPlayer.release();
            }
            player.sendMessage("All video streams stopped.");
            LOG.info("Video streams stopped for player: {}", player.getName());
        }
    }

    public void stop() {
        for (Collection<DirectMediaPlayer> players : activeMediaPlayers.values()) {
            for (DirectMediaPlayer mediaPlayer : players) {
                mediaPlayer.release();
            }
        }
        activeMediaPlayers.clear();
        mediaPlayerFactory.release();
    }
}
