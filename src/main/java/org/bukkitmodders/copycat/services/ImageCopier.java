package org.bukkitmodders.copycat.services;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkitmodders.copycat.plugin.RevertableBlock;
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
    private final Matrix4d mRotation;
    private final Matrix4d mWorld;
    private final TextureMapProcessor textureMapProcessor;

    public ImageCopier(TextureMapProcessor tmp, Location location, Matrix4d rotation) {

        textureMapProcessor = tmp;

        // Create and configure the transformation matrix with translation
        this.mWorld = new Matrix4d().identity();
        this.mWorld.translate(location.getX(), location.getY(), location.getZ());

        this.world = location.getWorld();
        this.mRotation = rotation;

        log.debug("Draw location set to: " + location.toString());
        log.debug("Transformation Matrix: " + mWorld.toString());
    }

    /**
     * This method draws the provided image in the minecraft world
     *
     * @param image
     */
    public void draw(BufferedImage image, Stack<RevertableBlock> undoBuffer) {

        Vector4d point = new Vector4d();

        for (int i = 0; i < image.getWidth(); i++) {

            for (int j = 0; j < image.getHeight(); j++) {

                // +Y in minecraft is the image top
                point.set(i, image.getHeight() - j, 0);
                //mRotation.transform(point);
                mRotation.transform(point);

                transformToWorld(point);
                Block blockAt = world.getBlockAt((int) Math.round(point.x), (int) (Math.round(point.y)), (int) Math.round(point.z));

                if (blockAt != null) {

                    if (undoBuffer != null) {
                        undoBuffer.push(new RevertableBlock(blockAt));
                    }

                    int rgba = image.getRGB(i, j);

                    int alpha = (rgba >> 24) & 0xff;

                    if (alpha == 0 || image.getTransparency() == Transparency.BITMASK) {
                        // If this is a transparent pixel, Do Nothing
                        // Maybe use glass or air??
                        blockAt.setType(Material.AIR);
                    } else {
                        int closestTile = findNearestTileForColorRGB(rgba, blockAt);
                        TextureToBlockMapper.setBlockMaterialToTile(closestTile, blockAt);
                    }
                }
            }
        }
    }

    /**
     * This method returns a texture tile that most closely matches the provided
     * RGBa value;
     *
     * @param rgba
     * @param block
     * @return
     */
    private int findNearestTileForColorRGB(int rgba, Block block) {

        double colorspaceDistance = Float.MAX_VALUE;
        int closestTile = -1;

        for (Color materialColor : textureMapProcessor.getColorTable().keySet()) {

            double currentDistance = getEuclidianDistance(new Color(rgba).getComponents(null), materialColor.getColorComponents(null));

            if (currentDistance < colorspaceDistance) {
                colorspaceDistance = currentDistance;
                closestTile = textureMapProcessor.getColorTable().get(materialColor);
            }
        }

        return closestTile;
    }

    double getEuclidianDistance(float[] componentsA, float[] componentsB) {
        float a = componentsA[0] - componentsB[0];
        float c = componentsA[1] - componentsB[1];
        float b = componentsA[2] - componentsB[2];

        return (float) Math.sqrt(a * a + b * b + c * c);
    }

    public void transformToWorld(Vector4d point) {
        mWorld.transform(point);
    }
}
