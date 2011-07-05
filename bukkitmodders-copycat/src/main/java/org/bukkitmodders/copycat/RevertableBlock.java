package org.bukkitmodders.copycat;

import org.bukkit.Material;
import org.bukkit.block.Block;

/**
 * A revertable block is a composite object that saves the state of a block on
 * construction so that future changes can be reverted
 * 
 * @author nroy
 * 
 */
public class RevertableBlock {

	private final byte oldData;
	private final Material oldType;
	private final Block block;

	public RevertableBlock(Block block) {
		this.block = block;

		oldData = block.getData();
		oldType = block.getType();
	}

	public void revert() {
		block.setType(oldType);
		block.setData(oldData);
	}
}
