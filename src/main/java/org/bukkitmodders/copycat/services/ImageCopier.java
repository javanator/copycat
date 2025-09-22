package org.bukkitmodders.copycat.services;

import java.awt.Color;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.util.Stack;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkitmodders.copycat.plugin.RevertableBlock;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageCopier {

	private static final Logger log = LoggerFactory.getLogger(ImageCopier.class);

	private final World world;
	private final Matrix3f mRotation;
	private Matrix3f mWorld;
	private TextureMapProcessor textureMapProcessor;

	public ImageCopier(TextureMapProcessor tmp, Location location, Matrix3f rotation) {
		// com.twelvemonkeys.image.ImageUtil.createIndexed(pImage, pColors,
		// pMatte, pHints)
		// new IndexColorModel(bits, size, r, g, b, a);

		textureMapProcessor = tmp;

		Vector3f translation = new Vector3f();
		translation.set(location.getX(), location.getY(), location.getZ());

		Matrix3f transform = new Matrix3f().identity();
		transform.transform(translation);

		this.world = location.getWorld();
		this.mWorld = transform;
		this.mRotation = rotation;

		log.debug("Draw location set to: " + translation.toString());
		log.debug("Transformation Matrix: " + transform.toString());
	}

	/**
	 * This method draws the provided image in the minecraft world
	 * 
	 * @param image
	 */
	public void draw(BufferedImage image, Stack<RevertableBlock> undoBuffer) {

		Vector3f point = new Vector3f();

		for (int i = 0; i < image.getWidth(); i++) {

			for (int j = 0; j < image.getHeight(); j++) {

				// +Y in minecraft is the image top
				point.set(i, image.getHeight() - j, 0);
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

	public void transformToWorld(Vector3f point) {
		mWorld.transform(point);
	}
}
