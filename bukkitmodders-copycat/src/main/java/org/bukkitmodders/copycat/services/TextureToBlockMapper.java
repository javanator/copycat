/**
 * This class's only function is to map tiles from the texture map to their respective materials. 
 */

package org.bukkitmodders.copycat.services;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkitmodders.copycat.schema.BlockProfileType;

public class TextureToBlockMapper {

	public static Hashtable<Integer, TextureMappedBlock> SUPPORTED_BLOCKS = new Hashtable<Integer, TextureMappedBlock>();
	private Map<Integer, TextureMappedBlock> supportedBlocks = new HashMap<Integer, TextureMappedBlock>();

	static {

		for (TextureMappedBlock block : TextureMappedBlock.values()) {
			SUPPORTED_BLOCKS.put(block.getTile(), block);
		}
	}

	public TextureToBlockMapper(BlockProfileType blockProfile) {
		for (org.bukkitmodders.copycat.schema.BlockProfileType.Block block : blockProfile.getBlock()) {

			int tileNumber = block.getTextureIndex();

			if (SUPPORTED_BLOCKS.containsKey(tileNumber)) {
				supportedBlocks.put(tileNumber, SUPPORTED_BLOCKS.get(tileNumber));
			} else {
				throw new RuntimeException("Tile " + tileNumber + " is not a supported block.");
			}
		}
	}

	public Set<Integer> getSupportedTiles() {
		return supportedBlocks.keySet();
	}
	
	public static void setBlockMaterialToTile(int tile, Block block) {

		if (SUPPORTED_BLOCKS.containsKey(tile)) {
			SUPPORTED_BLOCKS.get(tile).setBlock(block);
		} else {
			throw new RuntimeException("Cannot map texture tile to a block. " + tile);
		}
	}

	public enum TextureMappedBlock {

		NETHERACK(103, Material.NETHERRACK, (byte) 0),
		DIRT(2, Material.DIRT, (byte) 0),
		STONE(1, Material.STONE, (byte) 0),
		SANDSTONE(192, Material.SANDSTONE, (byte) 0),
		BRICK(7, Material.BRICK, (byte) 0),
		SNOW_BLOCK(66, Material.SNOW_BLOCK, (byte) 0),
		CLAY(72, Material.CLAY, (byte) 0),
		WOOD_PLANK(4, Material.WOOD, (byte) 0),
		IRON_BLOCK(22, Material.IRON_BLOCK, (byte) 0),
		GOLD_BLOCK(23, Material.GOLD_BLOCK, (byte) 0),
		DIAMOND_BLOCK(24, Material.DIAMOND_BLOCK, (byte) 0),
		GLOWSTONE(105, Material.GLOWSTONE, (byte) 0),
		LAPIS_LAZULI_BLOCK(144, Material.LAPIS_BLOCK, (byte) 0),
		LAPIS_LAZULI_ORE(160, Material.LAPIS_ORE, (byte) 0),
		OBSIDIAN(37, Material.OBSIDIAN, (byte) 0),
		NOTE_BLOCK(74, Material.NOTE_BLOCK, (byte) 0),
		MOSSY_COBBLESTONE(36, Material.MOSSY_COBBLESTONE, (byte) 0),
		IRON_ORE(33, Material.IRON_ORE, (byte) 0),
		COAL_ORE(34, Material.COAL_ORE, (byte) 0),
		DIAMOND_ORE(50, Material.DIAMOND_ORE, (byte) 0),
		GOLD_ORE(32, Material.GOLD_ORE, (byte) 0),
		REDSTONE_ORE(51, Material.REDSTONE_ORE, (byte) 0),
		COBBLESTONE(16, Material.COBBLESTONE, (byte) 0),

		ENDER_STONE(175, Material.ENDER_STONE, (byte) 0),
		SPONGE(48, Material.SPONGE, (byte) 0),
		NETHER_BRICK(224, Material.NETHER_BRICK, (byte) 0),

		WOOL_WHITE(64, Material.WOOL, DyeColor.WHITE.getData()),
		WOOL_BLACK(113, Material.WOOL, DyeColor.BLACK.getData()),
		WOOL_GRAY(114, Material.WOOL, DyeColor.GRAY.getData()),
		WOOL_RED(129, Material.WOOL, DyeColor.RED.getData()),
		WOOL_PINK(130, Material.WOOL, DyeColor.PINK.getData()),
		WOOL_GREEN(145, Material.WOOL, DyeColor.GREEN.getData()),
		WOOL_LIGHT_GREEN(146, Material.WOOL, DyeColor.LIME.getData()),
		WOOL_BROWN(161, Material.WOOL, DyeColor.BROWN.getData()),
		WOOL_YELLOW(162, Material.WOOL, DyeColor.YELLOW.getData()),
		WOOL_BLUE(177, Material.WOOL, DyeColor.BLUE.getData()),
		WOOL_LIGHT_BLUE(178, Material.WOOL, DyeColor.LIGHT_BLUE.getData()),
		WOOL_PURPLE(193, Material.WOOL, DyeColor.PURPLE.getData()),
		WOOL_LIGHT_PURPLE(194, Material.WOOL, DyeColor.MAGENTA.getData()),
		WOOL_CYAN(209, Material.WOOL, DyeColor.CYAN.getData()),
		WOOL_ORANGE(210, Material.WOOL, DyeColor.ORANGE.getData()),
		WOOL_LIGHT_GRAY(225, Material.WOOL, DyeColor.SILVER.getData());

		private final byte data;
		private final Material material;
		private final int tile;

		private TextureMappedBlock(int tile, Material material, byte data) {
			this.tile = tile;
			this.material = material;
			this.data = data;
		}

		public void setBlock(Block block) {

			block.setType(material);

			if (data != 0) {
				block.setData(data);
			}
		}

		public int getTile() {
			return tile;
		}

		public String getMaterialName() {
			return material.name();
		}
	}

}
