package org.bukkitmodders.copycat.commands;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkitmodders.copycat.Nouveau;
import org.bukkitmodders.copycat.managers.PlayerSettingsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminCommand implements CommandExecutor {

	private static Logger log = LoggerFactory.getLogger(AdminCommand.class);

	private final Nouveau plugin;

	public AdminCommand(Nouveau nouveau) {
		this.plugin = nouveau;
	}

	public static String getCommandString() {
		return "ccadmin";
	}

	public static Map<String, Object> getDescription() {
		StringBuffer sb = new StringBuffer();
		sb.append("/" + getCommandString() + " [ UNDO | UNDOOFF | UNDOON | UNDOPURGE ] <PLAYER> ");
		sb.append("\n UNDOON <PLAYER> - Enables the undo buffer for self or another player");
		sb.append("\n UNDOOFF <PLAYER> - Disables the undo buffer for self or another player. Speeds large renders");
		sb.append("\n UNDO <PLAYER> - Undoes another player's creation.");
		sb.append("\n UNDOPURGE [PLAYER]- Free memory. Clear undo buffer on a player or everyone");

		Map<String, Object> desc = new LinkedHashMap<String, Object>();
		desc.put("description", "Admin operations. Mostly griefing management");
		desc.put("usage", sb.toString());

		return desc;
	}

	public static Map<String, Object> getPermissions() {

		Map<String, Object> permissions = new LinkedHashMap<String, Object>();
		permissions.put("description", "Administrative and moderator Functions");
		permissions.put("default", "op");

		return permissions;
	}

	public static String getPermissionNode() {
		return "copycat.ccadmin";
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {

		if (!(sender instanceof Player))
			return true;

		Queue<String> argsQueue = new LinkedList<String>();
		argsQueue.addAll(Arrays.asList(args));
		String operation = argsQueue.poll();
		UndoCommand undoCommand = new UndoCommand(plugin);
		String targetPlayer = argsQueue.poll();

		if ("UNDO".equalsIgnoreCase(operation)) {
			if (targetPlayer == null) {
				undoCommand.performUndo(sender, targetPlayer);
				sender.sendMessage("Undo complete on player: " + targetPlayer);
			} else {
				undoCommand.performUndo(sender, sender.getName());
				sender.sendMessage("Undo complete");
			}
		} else if ("UNDOPURGE".equalsIgnoreCase(operation)) {
			if (argsQueue.isEmpty()) {
				PlayerSettingsManager.purgeAllUndoBuffers();
				sender.sendMessage("Purged undo buffers for everyone");
			} else {
				undoCommand.purgeUndoBuffer(targetPlayer);
			}
		} else if ("UNDOOFF".equalsIgnoreCase(operation)) {
			PlayerSettingsManager playerSettings = plugin.getConfigurationManager().getPlayerSettings(targetPlayer);
			playerSettings.setUndoEnabled(false);
		} else if ("UNDOON".equalsIgnoreCase(operation)) {
			PlayerSettingsManager playerSettings = plugin.getConfigurationManager().getPlayerSettings(targetPlayer);
			playerSettings.setUndoEnabled(true);
		}

		return true;
	}
}
