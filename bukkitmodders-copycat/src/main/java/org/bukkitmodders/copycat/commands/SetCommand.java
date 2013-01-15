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
import org.bukkitmodders.copycat.managers.PlayerSettingsManager;
import org.bukkitmodders.copycat.schema.BlockProfileType;
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
		sb.append("/" + getCommandString() + " [ DIM  | PROFILE <BLOCKPROFILE> | DITHERING ]");
		sb.append("\nDIM <WIDTH> <HEIGHT> - Scale copied images to this size");
		sb.append("\nPROFILE <BLOCK PROFILE> - Changes the active block profile");
		sb.append("\nDITHERING - toggles dithering on or off");
		sb.append("\n(no args) - View current settings");

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

			if (!sender.hasPermission(getPermissionNode())) {
				sender.sendMessage("You do not have permission: " + getPermissionNode());
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
			} else if ("PROFILE".equalsIgnoreCase(operation)) {
				String profile = argsQueue.poll();
				BlockProfileType blockProfile = plugin.getConfigurationManager().getBlockProfile(profile);
				if (blockProfile != null) {
					playerSettings.setBlockProfile(blockProfile.getName());
				}
				sender.sendMessage("Block profile changed to: " + blockProfile.getName());
			} else if ("DITHERING".equalsIgnoreCase(operation)) {
				playerSettings.setDithering(!playerSettings.isDithering());
				sender.sendMessage("Dithering set to " + playerSettings.isDithering());
			} else if (StringUtils.isBlank(operation)) {
				Map<String, BlockProfileType> blockProfiles = plugin.getConfigurationManager().getBlockProfiles();
				sender.sendMessage("Current Build Dimensions: " + playerSettings.getBuildWidth() + "x" + playerSettings.getBuildHeight());
				sender.sendMessage("Dithering: " + playerSettings.isDithering());
				sender.sendMessage("Block Profiles: " + Arrays.toString(blockProfiles.keySet().toArray()));
				sender.sendMessage("Active: " + playerSettings.getBlockProfile());
				sender.sendMessage("Rubber Stamp Mode: " + playerSettings.isStampModeActivated());
				sender.sendMessage("Rubber Stamp Item: " + playerSettings.getStampItem());
				sender.sendMessage("Rubber Stamp Image: " + playerSettings.getStampShortcut().getName());
			} else {
				return false;
			}

			return true;
		} catch (Exception e) {
			log.error("Something unexpected happened", e);
			sender.sendMessage(e.getMessage());
		}

		return false;
	}
}
