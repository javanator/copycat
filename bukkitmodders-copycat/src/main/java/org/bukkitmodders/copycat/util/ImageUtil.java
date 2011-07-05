package org.bukkitmodders.copycat.util;

import java.awt.image.BufferedImage;

import com.thebuzzmedia.imgscalr.Scalr;
import com.thebuzzmedia.imgscalr.Scalr.Method;

public class ImageUtil {

	/**
	 * 
	 * @param image
	 *            The image to be scaled
	 * @param imageType
	 *            Target image type, e.g. TYPE_INT_RGB
	 * @param newWidth
	 *            The required width
	 * @param newHeight
	 *            The required width
	 * 
	 * @return The scaled image
	 */
	public static BufferedImage scaleImage(BufferedImage image, int newWidth, int newHeight) {
		// Make sure the aspect ratio is maintained, so the image is not
		// distorted
		double thumbRatio = (double) newWidth / (double) newHeight;
		int imageWidth = image.getWidth(null);
		int imageHeight = image.getHeight(null);
		double aspectRatio = (double) imageWidth / (double) imageHeight;

		if (thumbRatio < aspectRatio) {
			newHeight = (int) (newWidth / aspectRatio);
		} else {
			newWidth = (int) (newHeight * aspectRatio);
		}

		return Scalr.resize(image, Method.QUALITY, newWidth, newHeight, Scalr.OP_ANTIALIAS);
	}
}
