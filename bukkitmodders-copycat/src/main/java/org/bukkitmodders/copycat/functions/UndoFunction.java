package org.bukkitmodders.copycat.functions;

import java.util.HashMap;
import java.util.Queue;
import java.util.Stack;

import org.bukkit.entity.Player;
import org.bukkitmodders.copycat.Plugin;
import org.bukkitmodders.copycat.RevertableBlock;

public class UndoFunction extends AbstractCopycatFunction {

	public static String FUNCTION_STRING = "undo";

	@Override
	public void performFunction(Player player, Queue<String> arg3) {

		doUndo(player, player.getName());
	}

	@Override
	public void buildFunctionHelp(StringBuffer sb) {
		sb.append(getOperationPrefix() + " - Undo last copy\n");
	}

	public UndoFunction(Plugin plugin) {
		super(plugin);
	}

	@Override
	public String getFunction() {
		return FUNCTION_STRING;
	}

	@Override
	protected boolean isNeedsOpPermissions() {
		return false;
	}

	public void doUndo(Player requestor, String playerName) {

		HashMap<String, Stack<Stack<RevertableBlock>>> undoBuffers = getPlugin().getUndoBuffers();

		if (undoBuffers.containsKey(playerName) && !undoBuffers.get(playerName).isEmpty()) {

			Stack<RevertableBlock> revertableBlocks = undoBuffers.get(playerName).pop();

			while (!revertableBlocks.isEmpty()) {
				revertableBlocks.pop().revert();
			}

			requestor.sendMessage("Performed Undo");
		} else {
			requestor.sendMessage("Nothing to Undo");
		}
	}
}
