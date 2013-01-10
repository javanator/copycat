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
import org.bukkitmodders.copycat.plugin.NeedMoreArgumentsException;
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

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		try {
			if (!(sender instanceof Player || args.length == 0)) {
				return false;
			}

			Queue<String> argsQueue = new LinkedList<String>();
			argsQueue.addAll(Arrays.asList(args));
			Player player = (Player) sender;

			ConfigurationManager configurationManager = plugin.getConfigurationManager();
			PlayerSettingsManager playerSettings = configurationManager.getPlayerSettings(player.getName());

			String operation = argsQueue.poll();

			if ("add".equalsIgnoreCase(operation)) {
				if (argsQueue.size() < 2) {
					throw new NeedMoreArgumentsException("No image name and URL in parameters. Received: " + Arrays.toString(args));
				}

				String imageName = argsQueue.poll();
				String imageUrl = argsQueue.poll();

				playerSettings.addShortcut(imageName, imageUrl);
			} else if ("del".equalsIgnoreCase(operation)) {
				if (argsQueue.size() < 1) {
					throw new NeedMoreArgumentsException("No image named in parameters. Received: " + Arrays.toString(args));
				}

				String imageName = argsQueue.poll();

				playerSettings.deleteShortcut(imageName);
			} else if ("clean".equalsIgnoreCase(operation)) {
				playerSettings.cleanShortcuts(player);
			} else if ("list".equalsIgnoreCase(operation)) {
				playerSettings.tellShortcuts(player);
			} else if ("copy".equalsIgnoreCase(operation)) {
				Location location = parseSpecifiedLocation(player, argsQueue);
			}

			return true;
		} catch (NeedMoreArgumentsException e) {
			sender.sendMessage(e.getMessage());
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
