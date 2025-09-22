package org.bukkitmodders.copycat.services;

import java.util.Hashtable;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;

public enum TextureMappedBlock {
	//AIR("AIR",207,Material.AIR,(byte)0),
	NETHERACK("NETHERACK",103, Material.NETHERRACK),
	DIRT("DIRT",2, Material.DIRT),
	STONE("STONE",1, Material.STONE),
	SANDSTONE("SANDSTONE",192, Material.SANDSTONE),
	BRICK("BRICK",7, Material.BRICK),
	SNOW_BLOCK("SNOW_BLOCK",66, Material.SNOW_BLOCK),
	CLAY("CLAY",72, Material.CLAY),
	LOG_OAK("LOG_OAK",20,Material.OAK_LOG),
	LOG_SPRUCE("LOG_SPRUCE",116,Material.SPRUCE_LOG),
	LOG_BIRCH("LOG_BIRCH",117,Material.BIRCH_LOG),
	LOG_JUNGLE("LOG_JUNGLE",153,Material.JUNGLE_LOG),
	WOOD_PLANK_OAK("WOOD_PLANK_OAK",4, Material.OAK_PLANKS),
	WOOD_PLANK_SPRUCE("WOOD_PLANK_SPRUCE",198, Material.SPRUCE_PLANKS),
	WOOD_PLANK_BIRCH("WOOD_PLANK_BIRCH",199, Material.BIRCH_PLANKS),
	WOOD_PLANK_JUNGLE("WOOD_PLANK_JUNGLE",214, Material.JUNGLE_PLANKS),
	IRON_BLOCK("IRON_BLOCK",22, Material.IRON_BLOCK),
	GOLD_BLOCK("GOLD_BLOCK",23, Material.GOLD_BLOCK),
	DIAMOND_BLOCK("DIAMOND_BLOCK",24, Material.DIAMOND_BLOCK),
	EMERALD_BLOCK("EMERALD_BLOCK",25, Material.EMERALD_BLOCK),
	GLOWSTONE("GLOWSTONE",105, Material.GLOWSTONE),
	LAPIS_LAZULI_BLOCK("LAPIS_LAZULI_BLOCK",144, Material.LAPIS_BLOCK),
	LAPIS_LAZULI_ORE("LAPIS_LAZULI_ORE",160, Material.LAPIS_ORE),
	OBSIDIAN("OBSIDIAN",37, Material.OBSIDIAN),
	NOTE_BLOCK("NOTE_BLOCK",74, Material.NOTE_BLOCK),
	MOSSY_COBBLESTONE("MOSSY_COBBLESTONE",36, Material.MOSSY_COBBLESTONE),
	IRON_ORE("IRON_ORE",33, Material.IRON_ORE),
	COAL_ORE("COAL_ORE",34, Material.COAL_ORE),
	DIAMOND_ORE("DIAMOND_ORE",50, Material.DIAMOND_ORE),
	GOLD_ORE("GOLD_ORE",32, Material.GOLD_ORE),
	REDSTONE_ORE("REDSTONE_ORE",51, Material.REDSTONE_ORE),
	COBBLESTONE("COBBLESTONE",16, Material.COBBLESTONE),
	ENDER_STONE("ENDER_STONE",175, Material.END_STONE_BRICKS),
	SPONGE("SPONGE",48, Material.SPONGE),
	NETHER_BRICK("NETHER_BRICK",224, Material.NETHER_BRICK),
	//ICE("ICE",67,Material.ICE,(byte) 0),
	REDSTONE_LAMP_OFF("REDSTONE_LAMP_OFF",211,Material.REDSTONE_LAMP),
	PUMPKIN("PUMPKIN",118,Material.JACK_O_LANTERN),
	MELON("MELON",136,Material.MELON),
	WOOL_WHITE("WOOL_WHITE",64, Material.WHITE_WOOL),
	WOOL_BLACK("WOOL_BLACK",113, Material.BLACK_WOOL),
	WOOL_GRAY("WOOL_GRAY",114, Material.GRAY_WOOL),
	WOOL_RED("WOOL_RED",129, Material.RED_WOOL),
	WOOL_PINK("WOOL_PINK",130, Material.PINK_WOOL),
	WOOL_GREEN("WOOL_GREEN",145, Material.GREEN_WOOL),
	WOOL_LIGHT_GREEN("WOOL_LIGHT_GREEN",146, Material.LIME_WOOL),
	WOOL_BROWN("WOOL_BROWN",161, Material.BROWN_WOOL),
	WOOL_YELLOW("WOOL_YELLOW",162, Material.YELLOW_WOOL),
	WOOL_BLUE("WOOL_BLUE",177, Material.BLUE_WOOL),
	WOOL_LIGHT_BLUE("WOOL_LIGHT_BLUE",178, Material.LIGHT_BLUE_WOOL),
	WOOL_PURPLE("WOOL_PURPLE",193, Material.PURPLE_WOOL),
	WOOL_LIGHT_PURPLE("WOOL_LIGHT_PURPLE",194, Material.MAGENTA_WOOL),
	WOOL_CYAN("WOOL_CYAN",209, Material.CYAN_WOOL),
	WOOL_ORANGE("WOOL_ORANGE",210, Material.ORANGE_WOOL),
	WOOL_LIGHT_GRAY("WOOL_LIGHT_GRAY",225, Material.LIGHT_GRAY_WOOL);

	private final Material material;
	private final int tile;
	private final String name;
	private static Hashtable<Integer, TextureMappedBlock> SUPPORTED_BLOCKS = new Hashtable<Integer, TextureMappedBlock>();
	
	static {
		for (TextureMappedBlock block : values()) {
			SUPPORTED_BLOCKS.put(block.getTile(), block);
		}
	}
	
	private TextureMappedBlock(String name, int tile, Material material) {
		this.tile = tile;
		this.material = material;
		this.name = name;
	}
	
	public TextureMappedBlock getBlock(int spriteIndex) {
		if (SUPPORTED_BLOCKS.containsKey(spriteIndex)) {
			return SUPPORTED_BLOCKS.get(spriteIndex);
		}
		
		return null;
	}

	public void setBlock(Block block) {
		block.setType(material);
	}
	
	public int getTile() {
		return tile;
	}

	public String getMaterialName() {
		return material.name();
	}
	
	public String getName() {
		return name;
	}

	public static TextureMappedBlock getBlockBySpriteIndex(int spriteIndex) {
		if (SUPPORTED_BLOCKS.containsKey(spriteIndex)) {
			return SUPPORTED_BLOCKS.get(spriteIndex);
		}
		
		return null;
	}
}