package org.bukkitmodders.copycat.util;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jhlabs.image.DiffusionFilter;
import com.jhlabs.image.DitherFilter;

public class ImageUtil {
	private static Logger log = LoggerFactory.getLogger(ImageUtil.class);

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
		int imageWidth = src.getWidth();
		int imageHeight = src.getHeight();
		double aspectRatio = (double) imageWidth / (double) imageHeight;

		if (thumbRatio < aspectRatio) {
			newHeight = (int) (newWidth / aspectRatio);
		} else {
			newWidth = (int) (newHeight * aspectRatio);
		}

		AffineTransform af = new AffineTransform();
		float xscale = (float) newWidth / imageWidth;
		float yscale = (float) newHeight / imageHeight;

		af.scale(xscale, yscale);

		AffineTransformOp operation = new AffineTransformOp(af, AffineTransformOp.TYPE_BICUBIC);
		BufferedImage bufferedThumbnail = operation.filter(src, null);
		
		return bufferedThumbnail;
	}

	public static IndexColorModel generateIndexColorModel(Set<Color> palette) {

		Set<Color> colors = new HashSet<Color>();
		// colors.add(new Color(0, 0, 0, 255));
		colors.addAll(palette);

		int size = colors.size();

		byte[] r = new byte[size];
		byte[] g = new byte[size];
		byte[] b = new byte[size];
		byte[] a = new byte[size];

		int i = 0;
		for (Color color : colors) {
			r[i] = (byte) color.getRed();
			g[i] = (byte) color.getGreen();
			b[i] = (byte) color.getBlue();
			a[i] = (byte) color.getAlpha();

			i++;
		}

		IndexColorModel cm = new IndexColorModel(8, colors.size(), r, g, b);

		return cm;
	}

	public static BufferedImage ditherImage(BufferedImage image, IndexColorModel icm) {

		DiffusionFilter df = new DiffusionFilter();
		df.setMatrix(DitherFilter.dither90Halftone6x6Matrix);
		BufferedImage filter = df.filter(image, null);
		
		return filter;
	}
}
