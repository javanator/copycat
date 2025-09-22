package org.bukkitmodders.copycat.services;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkitmodders.copycat.model.RevertibleBlock;
import org.joml.Matrix4d;
import org.joml.Vector4d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Stack;

public class ImageCopier {
    private static final Logger log = LoggerFactory.getLogger(ImageCopier.class);
    private final World world;
    private final Matrix4d rotationMatrix;
    private final Matrix4d worldMatrix;
    private final TextureMapProcessor textureMapProcessor;

    public ImageCopier(TextureMapProcessor textureMapProcessor, Location location, Matrix4d rotation) {
        this.textureMapProcessor = textureMapProcessor;

        // Create and configure the transformation matrix with translation
        this.worldMatrix = new Matrix4d().identity();
        this.worldMatrix.translate(location.getX(), location.getY(), location.getZ());

        this.world = location.getWorld();
        this.rotationMatrix = (rotation != null) ? rotation : new Matrix4d().identity();

        log.debug("Draw location set to: " + location.toString());
        log.debug("Transformation Matrix: " + worldMatrix.toString());
    }

    /**
     * This method draws the provided image in the minecraft world
     *
     * @param image      the image to draw
     * @param undoBuffer buffer to store revertable blocks for undo functionality
     */
    public void draw(BufferedImage image, Stack<RevertibleBlock> undoBuffer) {
        Vector4d point = new Vector4d();

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                processPixel(image, undoBuffer, point, x, y);
            }
        }
    }

    private void processPixel(BufferedImage image, Stack<RevertibleBlock> undoBuffer, Vector4d point, int x, int y) {
        Vector4d worldPosition = calculateWorldPosition(point, x, y, image.getHeight());
        Block block = getBlockAtPosition(worldPosition);

        if (block != null) {
            saveBlockForUndo(undoBuffer, block);
            setBlockFromImagePixel(image, block, x, y);
        }
    }

    private Vector4d calculateWorldPosition(Vector4d point, int x, int y, int imageHeight) {
        // +Y in minecraft is the image top
        point.set(x, imageHeight - y, 0);
        rotationMatrix.transform(point);
        transformToWorld(point);
        return point;
    }

    private Block getBlockAtPosition(Vector4d position) {
        int blockX = (int) Math.round(position.x);
        int blockY = (int) Math.round(position.y);
        int blockZ = (int) Math.round(position.z);
        return world.getBlockAt(blockX, blockY, blockZ);
    }

    private void saveBlockForUndo(Stack<RevertibleBlock> undoBuffer, Block block) {
        if (undoBuffer != null) {
            undoBuffer.push(new RevertibleBlock(block));
        }
    }

    private void setBlockFromImagePixel(BufferedImage image, Block block, int x, int y) {
        int rgba = image.getRGB(x, y);
        int alpha = (rgba >> 24) & 0xff;

        if (isTransparentPixel(alpha, image)) {
            block.setType(Material.AIR);
        } else {
            int closestTile = findNearestTileForColorRGB(rgba, block);
            TextureToBlockMapper.setBlockMaterialToTile(closestTile, block);
        }
    }

    private boolean isTransparentPixel(int alpha, BufferedImage image) {
        return alpha == 0 || image.getTransparency() == Transparency.BITMASK;
    }

    /**
     * This method returns a texture tile that most closely matches the provided
     * RGBA value;
     *
     * @param rgba  the RGBA color value
     * @param block the block (unused parameter - kept for compatibility)
     * @return the closest tile index
     */
    private int findNearestTileForColorRGB(int rgba, Block block) {
        double minDistance = Double.MAX_VALUE;
        int closestTile = -1;
        Color targetColor = new Color(rgba);

        for (Color materialColor : textureMapProcessor.getColorTable().keySet()) {
            double distance = getEuclidianDistance(
                    targetColor.getComponents(null),
                    materialColor.getColorComponents(null)
            );

            if (distance < minDistance) {
                minDistance = distance;
                closestTile = textureMapProcessor.getColorTable().get(materialColor);
            }
        }

        return closestTile;
    }

    double getEuclidianDistance(float[] componentsA, float[] componentsB) {
        float deltaR = componentsA[0] - componentsB[0];
        float deltaG = componentsA[1] - componentsB[1];
        float deltaB = componentsA[2] - componentsB[2];

        return Math.sqrt(deltaR * deltaR + deltaG * deltaG + deltaB * deltaB);
    }

    public void transformToWorld(Vector4d point) {
        worldMatrix.transform(point);
    }
}
