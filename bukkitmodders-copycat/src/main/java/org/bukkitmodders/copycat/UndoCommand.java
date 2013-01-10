package org.bukkitmodders.copycat;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingDeque;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.jline.internal.Log;
import org.bukkit.entity.Player;
import org.bukkitmodders.copycat.plugin.RevertableBlock;

public class UndoCommand implements CommandExecutor {
	private final Nouveau plugin;

	public UndoCommand(Nouveau nouveau) {
		this.plugin = nouveau;
	}

	public static String getCommandString() {
		return "ccundo";
	}

	public static Map<String, Object> getDescription() {

		StringBuffer sb = new StringBuffer();
		sb.append("/" + getCommandString() + " (no args) ");

		Map<String, Object> desc = new LinkedHashMap<String, Object>();
		desc.put("description", " Undoes an image copy.");
		desc.put("usage", sb.toString());

		return desc;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {

		if (!(sender instanceof Player)) {
			return false;
		}

		Player player = (Player) sender;
		String playerName = player.getName();

		try {
			HashMap<String, LinkedBlockingDeque<Stack<RevertableBlock>>> undoBuffers = plugin.getUndoBuffers();

			if (undoBuffers.containsKey(playerName) && !undoBuffers.get(playerName).isEmpty()) {

				Stack<RevertableBlock> revertableBlocks = undoBuffers.get(playerName).pop();

				while (!revertableBlocks.isEmpty()) {
					revertableBlocks.pop().revert();
				}

				player.sendMessage("Performed Undo");
			} else {
				player.sendMessage("Nothing to Undo");
			}
			return true;
		} catch (Exception e) {
			Log.error("Something unexpected happened", e);
		}

		return false;
	}

}
