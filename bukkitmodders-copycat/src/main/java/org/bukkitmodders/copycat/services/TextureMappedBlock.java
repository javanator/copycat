package org.bukkitmodders.copycat.services;

import java.util.Hashtable;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;

public enum TextureMappedBlock {

	NETHERACK("NETHERACK",103, Material.NETHERRACK, (byte) 0),
	DIRT("DIRT",2, Material.DIRT, (byte) 0),
	STONE("STONE",1, Material.STONE, (byte) 0),
	SANDSTONE("SANDSTONE",192, Material.SANDSTONE, (byte) 0),
	BRICK("BRICK",7, Material.BRICK, (byte) 0),
	SNOW_BLOCK("SNOW_BLOCK",66, Material.SNOW_BLOCK, (byte) 0),
	CLAY("CLAY",72, Material.CLAY, (byte) 0),
	LOG_OAK("LOG_OAK",20,Material.LOG,(byte)0xC),
	LOG_SPRUCE("LOG_SPRUCE",116,Material.LOG,(byte)0xC),
	LOG_BIRCH("LOG_BIRCH",117,Material.LOG,(byte)0xC),
	LOG_JUNGLE("LOG_JUNGLE",153,Material.LOG,(byte)0xC),
	WOOD_PLANK_OAK("WOOD_PLANK_OAK",4, Material.WOOD, (byte) 0),
	WOOD_PLANK_SPRUCE("WOOD_PLANK_SPRUCE",198, Material.WOOD, (byte) 1),
	WOOD_PLANK_BIRCH("WOOD_PLANK_BIRCH",199, Material.WOOD, (byte) 2),
	WOOD_PLANK_JUNGLE("WOOD_PLANK_JUNGLE",214, Material.WOOD, (byte) 3),
	IRON_BLOCK("IRON_BLOCK",22, Material.IRON_BLOCK, (byte) 0),
	GOLD_BLOCK("GOLD_BLOCK",23, Material.GOLD_BLOCK, (byte) 0),
	DIAMOND_BLOCK("DIAMOND_BLOCK",24, Material.DIAMOND_BLOCK, (byte) 0),
	EMERALD_BLOCK("EMERALD_BLOCK",25, Material.EMERALD_BLOCK, (byte) 0),
	GLOWSTONE("GLOWSTONE",105, Material.GLOWSTONE, (byte) 0),
	LAPIS_LAZULI_BLOCK("LAPIS_LAZULI_BLOCK",144, Material.LAPIS_BLOCK, (byte) 0),
	LAPIS_LAZULI_ORE("LAPIS_LAZULI_ORE",160, Material.LAPIS_ORE, (byte) 0),
	OBSIDIAN("OBSIDIAN",37, Material.OBSIDIAN, (byte) 0),
	NOTE_BLOCK("NOTE_BLOCK",74, Material.NOTE_BLOCK, (byte) 0),
	MOSSY_COBBLESTONE("MOSSY_COBBLESTONE",36, Material.MOSSY_COBBLESTONE, (byte) 0),
	IRON_ORE("IRON_ORE",33, Material.IRON_ORE, (byte) 0),
	COAL_ORE("COAL_ORE",34, Material.COAL_ORE, (byte) 0),
	DIAMOND_ORE("DIAMOND_ORE",50, Material.DIAMOND_ORE, (byte) 0),
	GOLD_ORE("GOLD_ORE",32, Material.GOLD_ORE, (byte) 0),
	REDSTONE_ORE("REDSTONE_ORE",51, Material.REDSTONE_ORE, (byte) 0),
	COBBLESTONE("COBBLESTONE",16, Material.COBBLESTONE, (byte) 0),
	ENDER_STONE("ENDER_STONE",175, Material.ENDER_STONE, (byte) 0),
	SPONGE("SPONGE",48, Material.SPONGE, (byte) 0),
	NETHER_BRICK("NETHER_BRICK",224, Material.NETHER_BRICK, (byte) 0),
	//ICE("ICE",67,Material.ICE,(byte) 0),
	REDSTONE_LAMP_OFF("REDSTONE_LAMP_OFF",211,Material.REDSTONE_LAMP_OFF,(byte) 0),
	//Cant get the damn cactus to work. Ends up as a tiny pickupable block
	//CACTUS("CACTUS",70,Material.CACTUS,(byte) 0),
	//JACKOLANTERN("JACKOLANTERN",120,Material.JACK_O_LANTERN,(byte)0x4),
	//PUMPKIN("PUMPKIN",118,Material.JACK_O_LANTERN,(byte)0x4),
	WOOL_WHITE("WOOL_WHITE",64, Material.WOOL, DyeColor.WHITE.getData()),
	WOOL_BLACK("WOOL_BLACK",113, Material.WOOL, DyeColor.BLACK.getData()),
	WOOL_GRAY("WOOL_GRAY",114, Material.WOOL, DyeColor.GRAY.getData()),
	WOOL_RED("WOOL_RED",129, Material.WOOL, DyeColor.RED.getData()),
	WOOL_PINK("WOOL_PINK",130, Material.WOOL, DyeColor.PINK.getData()),
	WOOL_GREEN("WOOL_GREEN",145, Material.WOOL, DyeColor.GREEN.getData()),
	WOOL_LIGHT_GREEN("WOOL_LIGHT_GREEN",146, Material.WOOL, DyeColor.LIME.getData()),
	WOOL_BROWN("WOOL_BROWN",161, Material.WOOL, DyeColor.BROWN.getData()),
	WOOL_YELLOW("WOOL_YELLOW",162, Material.WOOL, DyeColor.YELLOW.getData()),
	WOOL_BLUE("WOOL_BLUE",177, Material.WOOL, DyeColor.BLUE.getData()),
	WOOL_LIGHT_BLUE("WOOL_LIGHT_BLUE",178, Material.WOOL, DyeColor.LIGHT_BLUE.getData()),
	WOOL_PURPLE("WOOL_PURPLE",193, Material.WOOL, DyeColor.PURPLE.getData()),
	WOOL_LIGHT_PURPLE("WOOL_LIGHT_PURPLE",194, Material.WOOL, DyeColor.MAGENTA.getData()),
	WOOL_CYAN("WOOL_CYAN",209, Material.WOOL, DyeColor.CYAN.getData()),
	WOOL_ORANGE("WOOL_ORANGE",210, Material.WOOL, DyeColor.ORANGE.getData()),
	WOOL_LIGHT_GRAY("WOOL_LIGHT_GRAY",225, Material.WOOL, DyeColor.SILVER.getData());

	private final byte data;
	private final Material material;
	private final int tile;
	private final String name;
	private static Hashtable<Integer, TextureMappedBlock> SUPPORTED_BLOCKS = new Hashtable<Integer, TextureMappedBlock>();
	
	static {
		for (TextureMappedBlock block : values()) {
			SUPPORTED_BLOCKS.put(block.getTile(), block);
		}
	}
	
	private TextureMappedBlock(String name, int tile, Material material, byte data) {
		this.tile = tile;
		this.material = material;
		this.data = data;
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
		block.setData(data);
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