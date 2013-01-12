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

public class SetCommand implements CommandExecutor {

	private static Logger log = LoggerFactory.getLogger(SetCommand.class);

	private final Nouveau plugin;

	public SetCommand(Nouveau nouveau) {
		this.plugin = nouveau;
	}

	public static String getCommandString() {
		return "ccset";
	}

	public static Map<String, Object> getDescription() {
		StringBuffer sb = new StringBuffer();
		sb.append("/" + getCommandString() + " [ SETIMAGE | DIM ]");
		sb.append("\nDIM <WIDTH> <HEIGHT> - Scale copied images to this size");
		sb.append("\nSETIMAGE - Activates a named image to render when your magic item is used or you do not specify a name");
		sb.append("\n<imagename> - Convenience method. Same as SETIMAGE except you only supply an image name.");

		Map<String, Object> desc = new LinkedHashMap<String, Object>();
		desc.put("description", "Sets plugin properties");
		desc.put("usage", sb.toString());

		return desc;
	}

	public static Map<String, Object> getPermissions() {

		Map<String, Object> permissions = new LinkedHashMap<String, Object>();
		permissions.put("description", "Allows players to change their copycat settings");
		permissions.put("default", "true");

		return permissions;
	}

	public static String getPermissionNode() {
		return "copycat.ccset";
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		try {
			PlayerSettingsManager playerSettings = plugin.getConfigurationManager().getPlayerSettings(sender.getName());
			Queue<String> argsQueue = new LinkedList<String>();
			argsQueue.addAll(Arrays.asList(args));

			String operation = argsQueue.poll();

			if (operation == null) {
				return false;
			}

			if (!sender.hasPermission(getPermissionNode())) {
				sender.sendMessage("You do not have permission: " + getPermissionNode());
			} else if ("SETIMAGE".equalsIgnoreCase(operation)) {
				String imagename = argsQueue.poll();
				playerSettings.setActiveShortcut(imagename);
				sender.sendMessage("Set active image to: " + imagename);
			} else if (playerSettings.getShortcut(operation) != null) {
				playerSettings.setActiveShortcut(operation);
				sender.sendMessage("Set active image to: " + playerSettings.getActiveShortcut().getName());
			} else if ("DIM".equalsIgnoreCase(operation)) {

				String width = argsQueue.poll();
				String height = argsQueue.poll();

				if (width == null) {
					sender.sendMessage("Width not specified");
				} else if (height == null) {
					sender.sendMessage("Height not specified");
				}

				int widthInt = Integer.parseInt(width);
				int heightInt = Integer.parseInt(height);

				playerSettings.setBuildDimensions(widthInt, heightInt);
				sender.sendMessage("Set dimensions to " + widthInt + "x" + heightInt);
			}

			return true;
		} catch (Exception e) {
			log.error("Something unexpected happened", e);
			sender.sendMessage(e.getMessage());
		}

		return false;
	}
}
