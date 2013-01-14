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

public class ImgCommand implements CommandExecutor {
	static final Logger log = LoggerFactory.getLogger(ImgCommand.class);
	final Nouveau plugin;

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
		sb.append("/" + getCommandString() + " [ ADD | DEL | LIST | CLEAN | COPY ]");
		sb.append("\nADD <name> <url> - Add an image URL");
		sb.append("\nDEL <name> - Deletes an image URL by name");
		sb.append("\nLIST - Displays a list of your images");
		sb.append("\nCLEAN - Automatically cleans invalid images");
		sb.append("\nCOPY - Alternate copy method. Specify a location in the form of <NAME> X Y Z PITCH YAW <WORLD>");

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
	public boolean onCommand(final CommandSender sender, Command command, String alias, String[] args) {
		try {
			Queue<String> argsQueue = new LinkedList<String>();
			argsQueue.addAll(Arrays.asList(args));
			String operation = argsQueue.poll();

			ConfigurationManager configurationManager = plugin.getConfigurationManager();
			final PlayerSettingsManager playerSettings = configurationManager.getPlayerSettings(sender.getName());

			if (operation == null) {
				return false;
			}

			if (!sender.hasPermission(getPermissionNode())) {
				sender.sendMessage("You do not have permission");
			} else if ("add".equalsIgnoreCase(operation)) {

				String imageName = argsQueue.poll();
				String imageUrl = argsQueue.poll();

				if (StringUtils.isBlank(imageName) || StringUtils.isBlank(imageUrl)) {
					sender.sendMessage("No image name or URL specified");
				} else {
					playerSettings.addShortcut(imageName, imageUrl);
					sender.sendMessage(imageName + " added");
				}
			} else if ("del".equalsIgnoreCase(operation)) {

				String imageName = argsQueue.poll();

				if (StringUtils.isBlank(imageName)) {
					sender.sendMessage("No image name specified");
				}

				playerSettings.deleteShortcut(imageName);
			} else if ("clean".equalsIgnoreCase(operation)) {

				sender.sendMessage("Cleaning up your URLs. Removing bad URLs and non-images");
				plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

					@Override
					public void run() {
						playerSettings.cleanShortcuts();
						sender.sendMessage("Done with URL cleanup");
					}
				});
			} else if ("list".equalsIgnoreCase(operation)) {

				playerSettings.tellShortcuts(sender);
			} else {
				return false;
			}

			return true;
		} catch (Exception e) {
			sender.sendMessage("Something unexpected happened. Check your syntax.");
			log.error("Something Unexpected Happened", e);
		}

		return false;
	}

}
