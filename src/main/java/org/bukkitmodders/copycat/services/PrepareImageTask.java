package org.bukkitmodders.copycat.services;

import org.bukkitmodders.copycat.Application;
import org.bukkitmodders.copycat.managers.ConfigurationManager;
import org.bukkitmodders.copycat.managers.PlayerSettingsManager;
import org.bukkitmodders.copycat.model.BuildContext;
import org.bukkitmodders.copycat.model.RevertibleBlock;
import org.bukkitmodders.copycat.model.UndoHistoryComponent;
import org.bukkitmodders.copycat.util.ImageUtil;
import org.bukkitmodders.copycat.util.MatrixUtil;
import org.joml.Matrix4d;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.util.Deque;
import java.util.Stack;

public class PrepareImageTask {

    private final Application application;
    private final BuildContext buildContext;
    private final PlayerSettingsManager playerSettings;
    private final BufferedImage preparedImage;
    private final ImageCopier imageCopier;

    public PrepareImageTask(Application application, BuildContext buildContext) {
        TextureMapProcessor textureMapProcessor = new TextureMapProcessor(buildContext.getBlockProfile());
        Matrix4d rotationMatrix = MatrixUtil.calculateRotation(buildContext.getLocation());

        this.imageCopier = new ImageCopier(textureMapProcessor, buildContext.getLocation(), rotationMatrix);
        this.application = application;
        this.buildContext = buildContext;

        ConfigurationManager configurationManager = application.getConfigurationManager();
        playerSettings = configurationManager.getPlayerSettings(buildContext.getPlayer().getName());

        BufferedImage scaledImage = ImageUtil.scaleImage(buildContext.getImage(),
                playerSettings.getMaxBuildWidth(),
                playerSettings.getMaxBuildHeight());

        if (playerSettings.isDithering()) {
            IndexColorModel icm = ImageUtil.generateIndexColorModel(textureMapProcessor.getColorTable().keySet());
            scaledImage = ImageUtil.ditherImage(scaledImage, icm);
        }

        this.preparedImage = scaledImage;
    }

    public void performDraw() {
        Stack<RevertibleBlock> blocks = new Stack<>();

        try {
            blocks = imageCopier.createUndoBuffer(preparedImage.getWidth(), preparedImage.getHeight());
            imageCopier.draw(preparedImage, blocks);
        } finally {
            Deque<UndoHistoryComponent> undoBuffers = playerSettings.getUndoBuffer();
            undoBuffers.add(UndoHistoryComponent.builder()
                    .withBlocks(blocks)
                    .build());
            //TODO: Make the undo buffer persistent
        }
    }
}
