package org.bukkitmodders.copycat.commands;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkitmodders.copycat.Nouveau;
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

		return false;
	}
}
