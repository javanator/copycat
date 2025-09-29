package org.bukkitmodders.copycat.services;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkitmodders.copycat.Application;
import org.bukkitmodders.copycat.managers.ConfigurationManager;
import org.bukkitmodders.copycat.managers.PlayerSettingsManager;
import org.bukkitmodders.copycat.model.BlockProfileType;
import org.bukkitmodders.copycat.model.RevertibleBlock;
import org.bukkitmodders.copycat.util.ImageUtil;
import org.bukkitmodders.copycat.util.MatrixUtil;
import org.joml.Matrix4d;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingDeque;

public class CopyTask {
    private final BufferedImage image;
    private final CommandSender sender;
    private final Location location;

    public CopyTask(BufferedImage image, CommandSender sender, Location location) {
        this.image = image;
        this.location = location;
        this.sender = sender;
    }

    public void performDraw() {
        ConfigurationManager configurationManager = Application.getInstance().getConfigurationManager();
        PlayerSettingsManager senderSettings = configurationManager.getPlayerSettings(sender.getName());

        Stack<RevertibleBlock> undoBuffer = prepareUndoBuffer(senderSettings);

        try {
            ImageProcessingContext context = prepareImageProcessing(configurationManager, senderSettings);
            BufferedImage processedImage = processImage(context);
            renderImage(context, processedImage, undoBuffer);
            sendCompletionMessages(processedImage);
        } finally {
            saveUndoBuffer(senderSettings, undoBuffer);
        }
    }

    private Stack<RevertibleBlock> prepareUndoBuffer(PlayerSettingsManager senderSettings) {
        return senderSettings.isUndoEnabled() ? new Stack<>() : null;
    }

    private ImageProcessingContext prepareImageProcessing(ConfigurationManager configurationManager, PlayerSettingsManager senderSettings) {
        BlockProfileType blockProfile = configurationManager.getBlockProfile(sender.getName());
        TextureMapProcessor textureMapProcessor = new TextureMapProcessor(blockProfile);
        IndexColorModel icm = ImageUtil.generateIndexColorModel(textureMapProcessor.getColorTable().keySet());
        Matrix4d rotationMatrix = MatrixUtil.calculateRotation(location);
        ImageCopier imageCopier = new ImageCopier(textureMapProcessor, location, rotationMatrix);

        return new ImageProcessingContext(senderSettings, textureMapProcessor, icm, imageCopier);
    }

    private BufferedImage processImage(ImageProcessingContext context) {
        BufferedImage scaledImage = ImageUtil.scaleImage(image,
                context.senderSettings.getMaxBuildWidth(),
                context.senderSettings.getMaxBuildHeight());

        if (context.senderSettings.isDithering()) {
            scaledImage = ImageUtil.ditherImage(scaledImage, context.indexColorModel);
        }

        return scaledImage;
    }

    private void renderImage(ImageProcessingContext context, BufferedImage processedImage, Stack<RevertibleBlock> undoBuffer) {
        context.imageCopier.draw(processedImage, undoBuffer);
    }

    private void sendCompletionMessages(BufferedImage processedImage) {
        sender.sendMessage("Scaled Width: " + processedImage.getWidth() + " Scaled Height: " + processedImage.getHeight());
        sender.sendMessage("Rendered to (X,Y,Z) PITCH YAW WORLD): (" + location.getX() + "," + location.getY() + "," + location.getZ() + ") "
                + location.getPitch() + " " + location.getYaw() + " " + location.getWorld().getName());
        sender.sendMessage("Copycat Render complete");
    }

    private void saveUndoBuffer(PlayerSettingsManager senderSettings, Stack<RevertibleBlock> undoBuffer) {
        if (undoBuffer != null) {
            LinkedBlockingDeque<Stack<RevertibleBlock>> undoBuffers = senderSettings.getUndoBuffer();
            undoBuffers.push(undoBuffer);
        }
    }

    private static class ImageProcessingContext {
        final PlayerSettingsManager senderSettings;
        final TextureMapProcessor textureMapProcessor;
        final IndexColorModel indexColorModel;
        final ImageCopier imageCopier;

        ImageProcessingContext(PlayerSettingsManager senderSettings, TextureMapProcessor textureMapProcessor,
                               IndexColorModel indexColorModel, ImageCopier imageCopier) {
            this.senderSettings = senderSettings;
            this.textureMapProcessor = textureMapProcessor;
            this.indexColorModel = indexColorModel;
            this.imageCopier = imageCopier;
        }
    }
}
