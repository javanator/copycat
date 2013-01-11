package org.bukkitmodders.copycat.commands;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkitmodders.copycat.Nouveau;
import org.bukkitmodders.copycat.managers.ConfigurationManager;
import org.bukkitmodders.copycat.managers.PlayerSettingsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImgCommand implements CommandExecutor {
	private static final Logger log = LoggerFactory.getLogger(ImgCommand.class);
	private final Nouveau plugin;

	public ImgCommand(Nouveau nouveau) {
		this.plugin = nouveau;
	}

	public static String getCommandString() {
		return "ccimg";
	}

	/**
	 * Used during build time to generate the plugin.yml file. Not used during
	 * runtime.
	 * 
	 * @return
	 */
	public static Map<String, Object> getDescription() {

		StringBuffer sb = new StringBuffer();
		sb.append("/" + getCommandString() + " [ ADD | DEL | LIST | CLEAN ]");
		sb.append("\nADD <name>=<url> - Add an image URL");
		sb.append("\nDEL <name>- Deletes an image URL by name");
		sb.append("\nLIST - Displays a list of your images");
		sb.append("\nCLEAN - Automatically cleans invalid images");
		sb.append("\nCOPY - Alternate copy method. Specify a location in the form of X Y Z PITCH YAW");

		Map<String, Object> desc = new LinkedHashMap<String, Object>();
		desc.put("description", "Commands for various image operations");
		desc.put("usage", sb.toString());

		return desc;
	}

	public static Map<String, Object> getPermissions() {

		Map<String, Object> permissions = new LinkedHashMap<String, Object>();
		permissions.put("description", "Image management functions");
		permissions.put("default", "true");

		return permissions;
	}

	public static String getPermissionNode() {
		return "copycat.ccimg";
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		try {
			Queue<String> argsQueue = new LinkedList<String>();
			argsQueue.addAll(Arrays.asList(args));
			String operation = argsQueue.poll();

			if (!(sender instanceof Player || args.length == 0)) {
				return false;
			}

			if (operation == null) {
				return false;
			}

			if (!sender.hasPermission(getPermissionNode())) {
				sender.sendMessage("You do not have permission");
			}

			Player player = (Player) sender;

			ConfigurationManager configurationManager = plugin.getConfigurationManager();
			PlayerSettingsManager playerSettings = configurationManager.getPlayerSettings(player.getName());

			if ("add".equalsIgnoreCase(operation)) {

				String imageName = argsQueue.poll();
				String imageUrl = argsQueue.poll();

				if (imageName == null) {
					player.sendMessage("No image name specified");
				}

				if (imageUrl == null) {
					player.sendMessage("No image URL specified");
				}

				playerSettings.addShortcut(imageName, imageUrl);
				player.sendMessage(imageName + " added");
			} else if ("del".equalsIgnoreCase(operation)) {

				String imageName = argsQueue.poll();
				if (imageName == null) {
					player.sendMessage("No image name specified");
				}

				playerSettings.deleteShortcut(imageName);
			} else if ("clean".equalsIgnoreCase(operation)) {
				player.sendMessage("Cleaning up your URLs. Removing bad URLs and non-images");
				playerSettings.cleanShortcuts(player);
				player.sendMessage("Done with URL cleanup");
			} else if ("list".equalsIgnoreCase(operation)) {
				playerSettings.tellShortcuts(player);
			} else if ("copy".equalsIgnoreCase(operation)) {
				Location location = parseSpecifiedLocation(player, argsQueue);
			}

			return true;
		} catch (Exception e) {
			log.error("Something Unexpected Happened", e);
		}

		return false;
	}

	private Location parseSpecifiedLocation(Player requestor, Queue<String> args) {

		if (args.size() >= 5) {

			// The user has specified position manually

			int x = Integer.parseInt(args.remove());
			int y = Integer.parseInt(args.remove());
			int z = Integer.parseInt(args.remove());
			int yaw = Integer.parseInt(args.remove());
			int pitch = Integer.parseInt(args.remove());

			Location specifiedLocation = new Location(requestor.getWorld(), x, y, z);
			specifiedLocation.setYaw(yaw);
			specifiedLocation.setPitch(pitch);

			return specifiedLocation;
		}

		return null;
	}
}
