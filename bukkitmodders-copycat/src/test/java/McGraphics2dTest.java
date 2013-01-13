import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.vecmath.Point3d;
import javax.xml.bind.JAXB;

import junit.framework.Assert;

import org.bukkit.Location;
import org.bukkitmodders.copycat.Settings;
import org.bukkitmodders.copycat.managers.ConfigurationManager;
import org.bukkitmodders.copycat.schema.BlockProfileType;
import org.bukkitmodders.copycat.schema.BlockProfileType.Block;
import org.bukkitmodders.copycat.schema.PluginConfig;
import org.bukkitmodders.copycat.services.ImageCopier;
import org.bukkitmodders.copycat.services.TextureMapProcessor;
import org.bukkitmodders.copycat.services.TextureMappedBlock;
import org.bukkitmodders.copycat.services.TextureToBlockMapper;
import org.bukkitmodders.copycat.util.ImageUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class McGraphics2dTest {

	private Logger log = LoggerFactory.getLogger(McGraphics2dTest.class);

	@Test
	public void scaleImageTest() throws Exception {
		BufferedImage image = ImageIO.read(getClass().getResourceAsStream("/PikachuTransparent.gif"));
		Image scaledImage = ImageUtil.scaleImage(image, image.getWidth(), image.getHeight());
		ImageIO.write((RenderedImage) scaledImage, "gif", new File("target/scaledTransparent.gif"));
	}

	@Test
	public void TranslateTest01() {

		Location location = new Location(null, 10, 10, 10);
		ImageCopier mcGraphics2d = new ImageCopier(ConfigurationManager.generateDefaultBlockProfile(), location, null);

		Point3d point = new Point3d(1, 1, 0);

		log.debug("Original Point: " + point.toString());
		mcGraphics2d.transformToWorld(point);
		log.debug("Transformed Point: " + point.toString());

		Assert.assertEquals(11.0, point.x);
		Assert.assertEquals(11.0, point.y);
		Assert.assertEquals(10.0, point.z);
	}

	@Test
	public void TranslateTest02() {

		Location location = new Location(null, 10, 10, 10);
		
		ImageCopier mcGraphics2d = new ImageCopier(ConfigurationManager.generateDefaultBlockProfile(), location, null);

		Point3d point = new Point3d(10, 10, 0);

		log.debug("Original Point: " + point.toString());
		mcGraphics2d.transformToWorld(point);
		log.debug("Transformed Point: " + point.toString());

		Assert.assertEquals(20.0, point.x);
		Assert.assertEquals(20.0, point.y);
		Assert.assertEquals(10.0, point.z);
	}

	@Test
	public void transparencyTest() throws Exception {
		try {
			BufferedImage image = ImageIO.read(getClass().getResourceAsStream(Settings.PIKACHU_TRANSPARENT));
			if (image.getTransparency() == Transparency.BITMASK) {
				log.info("Transparency is bitmask");
			} else if (image.getTransparency() == Transparency.TRANSLUCENT) {
				log.info("Transparency is Translucent");
			} else if (image.getTransparency() == Transparency.OPAQUE) {
				throw new Exception("Image is opaque");
			}

		} catch (IOException e) {
			log.error("Error reading image", e);
		}
	}

	@Test
	public void nearestColorGeneratorTest() {

		TextureMapProcessor tmp01 = new TextureMapProcessor(ConfigurationManager.generateDefaultBlockProfile());
		tmp01.getColorTable();
	}

	@Test
	public void textureMapTest() throws Exception {
		BlockProfileType blockProfile = ConfigurationManager.generateDefaultBlockProfile();
		TextureMapProcessor tmp = new TextureMapProcessor(blockProfile);

		for (Block block : blockProfile.getBlock()) {
			BufferedImage tileImage = tmp.getTile(block.getTextureIndex());

			TextureMappedBlock textureMappedBlock = TextureMappedBlock.getBlockBySpriteIndex(block.getTextureIndex());
			String pathname = "target/" + block.getName() + "material-" + textureMappedBlock.getMaterialName() + ".gif";
			log.debug("Writing: " + pathname);
			ImageIO.write((RenderedImage) tileImage, "gif", new File(pathname));
		}

	}
}
