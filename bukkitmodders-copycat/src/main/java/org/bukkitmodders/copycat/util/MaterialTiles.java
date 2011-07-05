/**
 * This class's only function is to map tiles from the texture map to their respective materials. 
 */

package org.bukkitmodders.copycat.util;

import java.util.HashSet;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkitmodders.copycat.schema.BlockProfileType;

public class MaterialTiles {

	private static final int NETHERACK = 103;
	private static final int DIRT = 2;
	private static final int STONE = 1;
	private static final int SANDSTONE = 192;
	private static final int BRICK = 7;
	private static final int SNOW_BLOCK = 66;
	private static final int CLAY = 72;
	private static final int WOOD_PLANK = 4;
	private static final int IRON_BLOCK = 22;
	private static final int GOLD_BLOCK = 23;
	private static final int DIAMOND_BLOCK = 24;
	private static final int GLOWSTONE = 105;
	private static final int LAPIS_LAZULI_BLOCK = 144;
	private static final int LAPIS_LAZULI_ORE = 160;
	private static final int OBSIDIAN = 37;
	private static final int NOTE_BLOCK = 74;
	private static final int MOSSY_COBBLESTONE = 36;
	private static final int IRON_ORE = 33;
	private static final int COAL_ORE = 34;
	private static final int DIAMOND_ORE = 50;
	private static final int GOLD_ORE = 32;
	private static final int REDSTONE_ORE = 51;
	private static final int COBBLESTONE = 16;

	private static final int WOOL_WHITE = 64;
	private static final int WOOL_BLACK = 113;
	private static final int WOOL_GRAY = 114;
	private static final int WOOL_RED = 129;
	private static final int WOOL_PINK = 130;
	private static final int WOOL_GREEN = 145;
	private static final int WOOL_LIGHT_GREEN = 146;
	private static final int WOOL_BROWN = 161;
	private static final int WOOL_YELLOW = 162;
	private static final int WOOL_BLUE = 177;
	private static final int WOOL_LIGHT_BLUE = 178;
	private static final int WOOL_PURPLE = 193;
	private static final int WOOL_LIGHT_PURPLE = 194;
	private static final int WOOL_CYAN = 209;
	private static final int WOOL_ORANGE = 210;
	private static final int WOOL_LIGHT_GRAY = 225;

	private final HashSet<Integer> supportedTiles;

	public MaterialTiles(BlockProfileType blockProfile) {

		supportedTiles = new HashSet<Integer>();

		for (BlockProfileType.Block block : blockProfile.getBlock()) {
			supportedTiles.add(block.getTextureIndex());
		}
	}

	static {
	}

	public HashSet<Integer> getSupportedTiles() {
		return supportedTiles;
	}

	public static void setBlockMaterialToTile(int tile, Block block) {

		if (tile == WOOL_WHITE) {
			block.setType(Material.WOOL);
			block.setData(DyeColor.WHITE.getData());
		} else if (tile == WOOL_BLACK) {
			block.setType(Material.WOOL);
			block.setData(DyeColor.BLACK.getData());
		} else if (tile == WOOL_GRAY) {
			block.setType(Material.WOOL);
			block.setData(DyeColor.GRAY.getData());
		} else if (tile == WOOL_RED) {
			block.setType(Material.WOOL);
			block.setData(DyeColor.RED.getData());
		} else if (tile == WOOL_PINK) {
			block.setType(Material.WOOL);
			block.setData(DyeColor.PINK.getData());
		} else if (tile == WOOL_GREEN) {
			block.setType(Material.WOOL);
			block.setData(DyeColor.GREEN.getData());
		} else if (tile == WOOL_LIGHT_GREEN) {
			block.setType(Material.WOOL);
			block.setData(DyeColor.LIME.getData());
		} else if (tile == WOOL_BROWN) {
			block.setType(Material.WOOL);
			block.setData(DyeColor.BROWN.getData());
		} else if (tile == WOOL_YELLOW) {
			block.setType(Material.WOOL);
			block.setData(DyeColor.YELLOW.getData());
		} else if (tile == WOOL_BLUE) {
			block.setType(Material.WOOL);
			block.setData(DyeColor.BLUE.getData());
		} else if (tile == WOOL_LIGHT_BLUE) {
			block.setType(Material.WOOL);
			block.setData(DyeColor.LIGHT_BLUE.getData());
		} else if (tile == WOOL_PURPLE) {
			block.setType(Material.WOOL);
			block.setData(DyeColor.PURPLE.getData());
		} else if (tile == WOOL_LIGHT_PURPLE) {
			block.setType(Material.WOOL);
			block.setData(DyeColor.MAGENTA.getData());
		} else if (tile == WOOL_CYAN) {
			block.setType(Material.WOOL);
			block.setData(DyeColor.CYAN.getData());
		} else if (tile == WOOL_ORANGE) {
			block.setType(Material.WOOL);
			block.setData(DyeColor.ORANGE.getData());
		} else if (tile == WOOL_LIGHT_GRAY) {
			block.setType(Material.WOOL);
			block.setData(DyeColor.SILVER.getData());
		} else if (tile == COBBLESTONE) {
			block.setType(Material.COBBLESTONE);
		} else if (tile == NETHERACK) {
			block.setType(Material.NETHERRACK);
		} else if (tile == DIRT) {
			block.setType(Material.DIRT);
		} else if (tile == STONE) {
			block.setType(Material.STONE);
		} else if (tile == SANDSTONE) {
			block.setType(Material.SANDSTONE);
		} else if (tile == BRICK) {
			block.setType(Material.BRICK);
		} else if (tile == SNOW_BLOCK) {
			block.setType(Material.SNOW_BLOCK);
		} else if (tile == WOOD_PLANK) {
			block.setType(Material.WOOD);
		} else if (tile == CLAY) {
			block.setType(Material.CLAY);
		} else if (tile == IRON_BLOCK) {
			block.setType(Material.IRON_BLOCK);
		} else if (tile == GOLD_BLOCK) {
			block.setType(Material.GOLD_BLOCK);
		} else if (tile == DIAMOND_BLOCK) {
			block.setType(Material.DIAMOND_BLOCK);
		} else if (tile == GLOWSTONE) {
			block.setType(Material.GLOWSTONE);
		} else if (tile == REDSTONE_ORE) {
			block.setType(Material.REDSTONE_ORE);
		} else if (tile == LAPIS_LAZULI_BLOCK) {
			block.setType(Material.LAPIS_BLOCK);
		} else if (tile == LAPIS_LAZULI_ORE) {
			block.setType(Material.LAPIS_ORE);
		} else if (tile == NOTE_BLOCK) {
			block.setType(Material.NOTE_BLOCK);
		} else if (tile == MOSSY_COBBLESTONE) {
			block.setType(Material.MOSSY_COBBLESTONE);
		} else if (tile == OBSIDIAN) {
			block.setType(Material.OBSIDIAN);
		} else if (tile == IRON_ORE) {
			block.setType(Material.IRON_ORE);
		} else if (tile == COAL_ORE) {
			block.setType(Material.COAL_ORE);
		} else if (tile == DIAMOND_ORE) {
			block.setType(Material.DIAMOND_ORE);
		} else if (tile == GOLD_ORE) {
			block.setType(Material.GOLD_ORE);
		} else {
			throw new RuntimeException("Unsupported Tile Number: " + tile);
		}
	}
}
