package org.bukkitmodders.copycat.util;

import java.awt.Color;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.util.Stack;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkitmodders.copycat.RevertableBlock;
import org.bukkitmodders.copycat.schema.BlockProfileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageCopier {

	private Logger log = LoggerFactory.getLogger(ImageCopier.class);

	private final World world;
	private final Matrix4d mOrientation;
	private Matrix4d mWorld;
	private TextureMapProcessor textureMapProcessor;

	public ImageCopier(BlockProfileType blockProfile, Location location, World world, Matrix4d localTransform) {

		textureMapProcessor = new TextureMapProcessor(blockProfile);

		Vector3d translation = new Vector3d();
		translation.set(location.getX(), location.getY(), location.getZ());

		Matrix4d transform = new Matrix4d();
		transform.setIdentity();
		transform.setTranslation(translation);

		this.world = world;
		this.mWorld = transform;
		this.mOrientation = localTransform;

		log.debug("Draw location set to: " + translation.toString());
		log.debug("Transformation Matrix: " + transform.toString());
	}

	/**
	 * This method draws the provided image in the minecraft world
	 * 
	 * @param image
	 */
	public void draw(BufferedImage image, Stack<RevertableBlock> undoBuffer) {

		for (int i = 0; i < image.getWidth(); i++) {

			for (int j = 0; j < image.getHeight(); j++) {

				// +Y in minecraft is the image top
				Point3d point = new Point3d(i, image.getHeight() - j, 0);
				mOrientation.transform(point);

				transformToWorld(point);
				Block blockAt = world.getBlockAt((int) Math.round(point.x), (int) (Math.round(point.y)),
						(int) Math.round(point.z));

				synchronized (blockAt) {
					if (blockAt != null) {

						undoBuffer.push(new RevertableBlock(blockAt));

						int rgba = image.getRGB(i, j);
						int alpha = (rgba >> 24) & 0xff;

						if (alpha == 0 || image.getTransparency() == Transparency.BITMASK) {
							// If this is a transparent pixel, Do Nothing
							// Maybe use glass or air??
						} else {
							int closestTile = findNearestTileForColor(rgba, blockAt);
							MaterialTiles.setBlockMaterialToTile(closestTile, blockAt);
						}
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
	private int findNearestTileForColor(int rgba, Block block) {

		double colorspaceDistance = Float.MAX_VALUE;
		int closestTile = -1;

		for (Color materialColor : textureMapProcessor.getColorTable().keySet()) {

			double currentDistance = ColorUtil.colorDistance(new Color(rgba), materialColor);

			if (currentDistance < colorspaceDistance) {
				colorspaceDistance = currentDistance;
				closestTile = textureMapProcessor.getColorTable().get(materialColor);
			}
		}

		return closestTile;
	}

	public void transformToWorld(Point3d point) {
		mWorld.transform(point);
	}
}
