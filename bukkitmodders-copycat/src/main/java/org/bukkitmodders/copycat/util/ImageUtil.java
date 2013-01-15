package org.bukkitmodders.copycat.util;

import java.awt.Color;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.IndexColorModel;
import java.util.Set;

import com.twelvemonkeys.image.DiffusionDither;

public class ImageUtil {

	/**
	 * 
	 * @param src
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
	public static BufferedImage scaleImage(BufferedImage src, int newWidth, int newHeight) {
		// Make sure the aspect ratio is maintained, so the image is not
		// distorted
		double thumbRatio = (double) newWidth / (double) newHeight;
		int imageWidth = src.getWidth(null);
		int imageHeight = src.getHeight(null);
		double aspectRatio = (double) imageWidth / (double) imageHeight;

		if (thumbRatio < aspectRatio) {
			newHeight = (int) (newWidth / aspectRatio);
		} else {
			newWidth = (int) (newHeight * aspectRatio);
		}

		int imageType = (src.getTransparency() == Transparency.OPAQUE ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB);
		Image scaledInstance = src.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);

		BufferedImage bufferedThumbnail = new BufferedImage(newWidth, newHeight, imageType);
		bufferedThumbnail.getGraphics().drawImage(scaledInstance, 0, 0, null);

		return bufferedThumbnail;
	}

	public static IndexColorModel generateIndexColorModel(Set<Color> palette) {

		int size = palette.size();

		byte[] r = new byte[size];
		byte[] g = new byte[size];
		byte[] b = new byte[size];
		byte[] a = new byte[size];

		int i = 0;
		for (Color color : palette) {
			r[i] = (byte) color.getRed();
			g[i] = (byte) color.getGreen();
			b[i] = (byte) color.getBlue();
			a[i] = (byte) color.getAlpha();

			i++;
		}

		IndexColorModel cm = new IndexColorModel(8, palette.size(), r, g, b);

		return cm;
	}

	public static BufferedImage ditherImage(BufferedImage image, IndexColorModel icm) {
		
		DiffusionDither diffusionDither = new com.twelvemonkeys.image.DiffusionDither(icm);
		BufferedImage dest = diffusionDither.createCompatibleDestImage(image, icm);
		BufferedImage filter = diffusionDither.filter(image, dest);

		return filter;
	}
}
