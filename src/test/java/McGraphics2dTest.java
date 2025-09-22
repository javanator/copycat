import junit.framework.Assert;
import org.apache.commons.io.FilenameUtils;
import org.bukkit.Location;
import org.bukkitmodders.copycat.Settings;
import org.bukkitmodders.copycat.managers.ConfigurationManager;
import org.bukkitmodders.copycat.model.BlockProfileType;
import org.bukkitmodders.copycat.model.BlockProfileType.Block;
import org.bukkitmodders.copycat.services.ImageCopier;
import org.bukkitmodders.copycat.services.TextureMapProcessor;
import org.bukkitmodders.copycat.services.TextureMappedBlock;
import org.bukkitmodders.copycat.util.ImageUtil;
import org.joml.Vector4d;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.Set;

public class McGraphics2dTest {

    private Logger log = LoggerFactory.getLogger(McGraphics2dTest.class);

//    @Test
    public void scaleImageTest() throws Exception {

        scaleAndDither("/transparent.png");
        scaleAndDither("/halle.jpg");
        scaleAndDither("/PikachuTransparent.gif");
        scaleAndDither("/hulk.jpg");
    }

    private void scaleAndDither(String imageRelativePath) throws IOException {

        String baseName = FilenameUtils.getBaseName(imageRelativePath);
        String extension = FilenameUtils.getExtension(imageRelativePath);

        BufferedImage image = ImageIO.read(getClass().getResourceAsStream(imageRelativePath));

        BufferedImage scaledImage = ImageUtil.scaleImage(image, 200, 200);

        ImageIO.write((RenderedImage) scaledImage, "png", new File("target/" + baseName + "-scaled." + extension));

        TextureMapProcessor textureMapProcessor = new TextureMapProcessor(ConfigurationManager.generateDefaultBlockProfile());
        Set<Color> palette = textureMapProcessor.getColorTable().keySet();

        IndexColorModel icm = ImageUtil.generateIndexColorModel(palette);
        Image ditheredImage = ImageUtil.ditherImage(scaledImage, icm);

        ImageIO.write((RenderedImage) ditheredImage, "png", new File("target/" + baseName + "-dithered." + extension));

        ImageIO.write((RenderedImage) image, "png", new File("target/" + baseName + "-original." + extension));
    }

    @Test
    public void TranslateTest01() {
        Location location = new Location(null, 10, 10, 10);
        BlockProfileType generateDefaultBlockProfile = ConfigurationManager.generateDefaultBlockProfile();
        TextureMapProcessor textureMapProcessor = new TextureMapProcessor(generateDefaultBlockProfile);
        ImageCopier mcGraphics2d = new ImageCopier(textureMapProcessor, location, null);

        // Fix: Set w = 1 for a position point (not w = 0)
        Vector4d point = new Vector4d(1, 1, 0, 1);

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
        BlockProfileType generateDefaultBlockProfile = ConfigurationManager.generateDefaultBlockProfile();
        TextureMapProcessor textureMapProcessor = new TextureMapProcessor(generateDefaultBlockProfile);
        ImageCopier mcGraphics2d = new ImageCopier(textureMapProcessor, location, null);

        // Fix: Set w = 1 for a position point
        Vector4d point = new Vector4d(10, 10, 0, 1);

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

        // Ensure target directory exists
        File targetDir = new File("build/blocks/");
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        for (Block block : blockProfile.getBlock()) {
            BufferedImage tileImage = tmp.getTile(block.getTextureIndex());

            TextureMappedBlock textureMappedBlock = TextureMappedBlock.getBlockBySpriteIndex(block.getTextureIndex());
            
            // Clean the filename to avoid invalid characters
            String cleanBlockName = block.getName().replaceAll("[^a-zA-Z0-9._-]", "_");
            String cleanMaterialName = textureMappedBlock.getMaterialName().replaceAll("[^a-zA-Z0-9._-]", "_");
            
            String pathname = targetDir.getPath()+File.separatorChar + cleanBlockName + "material-" + cleanMaterialName + ".png";
            log.debug("Writing: " + pathname);
            
            // Convert to RGB format for better compatibility and use PNG instead of GIF
            BufferedImage rgbImage = new BufferedImage(tileImage.getWidth(), tileImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = rgbImage.createGraphics();
            g.setColor(Color.WHITE); // Set background color for transparency
            g.fillRect(0, 0, tileImage.getWidth(), tileImage.getHeight());
            g.drawImage(tileImage, 0, 0, null);
            g.dispose();
            
            ImageIO.write(rgbImage, "png", new File(pathname));
        }
    }
}
