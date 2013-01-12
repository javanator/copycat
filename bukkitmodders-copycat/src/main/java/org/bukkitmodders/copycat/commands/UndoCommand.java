package org.bukkitmodders.copycat.commands;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingDeque;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkitmodders.copycat.Nouveau;
import org.bukkitmodders.copycat.managers.PlayerSettingsManager;
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

	public static Map<String, Object> getPermissions() {

		Map<String, Object> permissions = new LinkedHashMap<String, Object>();
		permissions.put("description", "Undo capability");
		permissions.put("default", "true");

		return permissions;
	}

	public static String getPermissionNode() {
		return "copycat.ccundo";
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {

		if (!(sender instanceof Player)) {
			return true;
		}

		if (!sender.hasPermission(getPermissionNode())) {
			sender.sendMessage("You do not have permission: " + getPermissionNode());
			return true;
		}

		Player player = (Player) sender;
		String playerName = player.getName();

		if (performUndo(player, playerName)) {
			sender.sendMessage("Performed Undo");
		} else {
			sender.sendMessage("Nothing to Undo");
		}

		return true;
	}

	public boolean performUndo(CommandSender sender, String playerName) {
		PlayerSettingsManager playerSettings = plugin.getConfigurationManager().getPlayerSettings(playerName);

		LinkedBlockingDeque<Stack<RevertableBlock>> undoBuffer = playerSettings.getUndoBuffer();

		if (!undoBuffer.isEmpty()) {
			Stack<RevertableBlock> lastImageBlocks = undoBuffer.pop();

			while (!lastImageBlocks.isEmpty()) {
				lastImageBlocks.pop().revert();
			}

			return true;
		}

		return false;
	}

	/**
	 * Purges the undo buffer of the target player.
	 * 
	 * @param targetPlayer
	 */
	public void purgeUndoBuffer(String targetPlayer) {
		PlayerSettingsManager playerSettings = plugin.getConfigurationManager().getPlayerSettings(targetPlayer);
		playerSettings.getUndoBuffer().clear();
	}
}
