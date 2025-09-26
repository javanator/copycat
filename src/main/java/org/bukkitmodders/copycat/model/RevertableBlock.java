package org.bukkitmodders.copycat.model;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

/**
 * A revertable block is a composite object that saves the state of a block on
 * construction so that future changes can be reverted
 * 
 * @author nroy
 * 
 */
public class RevertableBlock {

	private final BlockData oldData;
	private final Material oldType;
	private final Block block;

	public RevertableBlock(Block block) {
		this.block = block;

		oldData = block.getBlockData();
		oldType = block.getType();
	}

	public void revert() {
		block.setType(oldType);
		block.setBlockData(oldData);
	}
}
