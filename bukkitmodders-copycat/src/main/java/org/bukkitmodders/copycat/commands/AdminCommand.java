package org.bukkitmodders.copycat.commands;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkitmodders.copycat.Nouveau;
import org.bukkitmodders.copycat.managers.ConfigurationManager;
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
		sb.append("\n UNDOPURGE <PLAYER>- Free memory. Clear undo buffer on a player or everyone");

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

		ConfigurationManager configurationManager = plugin.getConfigurationManager();

		Queue<String> argsQueue = new LinkedList<String>();
		argsQueue.addAll(Arrays.asList(args));
		String operation = argsQueue.poll();
		CCCommand undoCommand = new CCCommand(plugin);
		String targetPlayer = argsQueue.poll();

		if ("UNDO".equalsIgnoreCase(operation)) {
			if (!StringUtils.isBlank(targetPlayer)) {
				undoCommand.performUndo(sender, targetPlayer);
				sender.sendMessage("Undo complete on player: " + targetPlayer);
			} else {
				undoCommand.performUndo(sender, sender.getName());
				sender.sendMessage("Undo complete on player: " + sender.getName());
			}
		} else if ("UNDOPURGE".equalsIgnoreCase(operation)) {
			PlayerSettingsManager.purgeAllUndoBuffers();
			sender.sendMessage("Purged undo buffers for everyone");
		} else if ("UNDOOFF".equalsIgnoreCase(operation)) {
			if (!StringUtils.isBlank(targetPlayer)) {
				PlayerSettingsManager playerSettings = configurationManager.getPlayerSettings(targetPlayer);
				playerSettings.setUndoEnabled(false);
				sender.sendMessage("Turned off undo buffer");
			}
		} else if ("UNDOON".equalsIgnoreCase(operation)) {
			PlayerSettingsManager playerSettings = configurationManager.getPlayerSettings(targetPlayer);
			playerSettings.setUndoEnabled(true);
			sender.sendMessage("Turned on undo buffer");
		} else {
			return (false);
		}

		return true;
	}
}
