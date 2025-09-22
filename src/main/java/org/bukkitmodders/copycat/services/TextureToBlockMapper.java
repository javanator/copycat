/**
 * This class's only function is to map tiles from the texture map to their respective materials. 
 */

package org.bukkitmodders.copycat.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.block.Block;
import org.bukkitmodders.copycat.model.BlockProfileType;

public class TextureToBlockMapper {

	private Map<Integer, TextureMappedBlock> supportedBlocks = new HashMap<Integer, TextureMappedBlock>();

	public TextureToBlockMapper(BlockProfileType blockProfile) {
		for (BlockProfileType.Block block : blockProfile.getBlock()) {

			int tileNumber = block.getTextureIndex();
			TextureMappedBlock sprite = TextureMappedBlock.getBlockBySpriteIndex(tileNumber);

			if (sprite != null) {
				supportedBlocks.put(tileNumber, sprite);
			} else {
				throw new RuntimeException("Tile " + tileNumber + " is not a supported block.");
			}
		}
	}

	public Set<Integer> getSupportedTiles() {
		return supportedBlocks.keySet();
	}

	public static void setBlockMaterialToTile(int tile, Block block) {

		TextureMappedBlock blockBySpriteIndex = TextureMappedBlock.getBlockBySpriteIndex(tile);
		if (blockBySpriteIndex != null) {
			blockBySpriteIndex.setBlock(block);
		} else {
			throw new RuntimeException("Cannot map texture tile to a block. " + tile);
		}
	}
}
