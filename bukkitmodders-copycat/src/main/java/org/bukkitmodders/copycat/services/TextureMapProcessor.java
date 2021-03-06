package org.bukkitmodders.copycat.services;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import org.bukkitmodders.copycat.Settings;
import org.bukkitmodders.copycat.schema.BlockProfileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextureMapProcessor {

	private static final Logger log = LoggerFactory.getLogger(TextureMapProcessor.class);

	private String textureResource = Settings.TERRAIN;
	private int textureWidth = 16;
	private int textureHeight = 16;
	private BufferedImage image;
	private int rows;
	private int cols;
	private int tiles;
	private Hashtable<Color, Integer> colorTable;

	private final BlockProfileType blockProfile;

	public TextureMapProcessor(BlockProfileType blockProfile) {

		this.blockProfile = blockProfile;

		try {
			image = getImage();
			rows = image.getHeight() / textureHeight;
			cols = image.getWidth() / textureWidth;

			tiles = (rows * cols);
			colorTable = generateColorTable();

			log.debug("Rows: " + rows);
			log.debug("Cols: " + cols);
			log.debug("Number of Tiles: " + tiles);

		} catch (IOException e) {
			log.error("Error loading texture file: " + textureResource, e);
		}
	}

	private Hashtable<Color, Integer> generateColorTable() {

		TextureToBlockMapper materialTiles = new TextureToBlockMapper(blockProfile);
		Hashtable<Color, Integer> properties = new Hashtable<Color, Integer>();
		log.debug("Generating color table for block profile: " + blockProfile.getName());
		for (int i = 0; i < tiles; i++) {
			BufferedImage tile = getTile(i);

			Color color = getAverageRGBColor(tile);

			if (materialTiles.getSupportedTiles().contains(i)) {
				properties.put(color, i);
				log.debug("Tile: " + i + " Average Color: " + color.toString() + " Alpha: " + color.getAlpha());
			}
		}
		
		return properties;
	}

	private Color getAverageRGBColor(BufferedImage tile) {

		int pixels = tile.getWidth() * tile.getHeight();

		int r = 0;
		int g = 0;
		int b = 0;
		int a = 0;

		for (int x = 0; x < tile.getWidth(); x++) {
			for (int y = 0; y < tile.getHeight(); y++) {

				int rgb = tile.getRGB(x, y);

				Color color = new Color(rgb);

				r += color.getRed();
				g += color.getGreen();
				b += color.getBlue();
				a += color.getAlpha();
			}
		}

		r /= pixels;
		g /= pixels;
		b /= pixels;
		a /= pixels;

		return new Color(r, g, b, a);
	}

	public BufferedImage getTile(int tileNumber) {

		int row = tileNumber / rows;
		int col = tileNumber % cols;

		return getTile(row, col);
	}

	private BufferedImage getTile(int row, int col) {

		int x = textureWidth * col;
		int y = textureHeight * row;
		BufferedImage tile = new BufferedImage(textureWidth, textureHeight, BufferedImage.TYPE_INT_ARGB);
		tile.getGraphics().drawImage(image.getSubimage(x, y, textureWidth, textureHeight), 0, 0, new Color(0, 0, 0, 255), null);

		return tile;
	}

	private BufferedImage getImage() throws IOException {

		return ImageIO.read(getClass().getResourceAsStream(Settings.TERRAIN));
	}

	public Hashtable<Color, Integer> getColorTable() {
		return colorTable;
	}
}
