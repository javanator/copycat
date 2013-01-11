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
import org.bukkit.inventory.ItemStack;
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
		sb.append("/" + getCommandString() + " [ ON | OFF | SELECT | TRIGGER | SCALE ]");
		sb.append("\nON - Enables image copy when the trigger is in the player's hand");
		sb.append("\nOFF - Disables image copy when the trigger is in the player's hand");
		sb.append("\nTRIGGER - Sets the activation item. Defaults to fist");
		sb.append("\nSELECT <imagename>- Pick an image from the list of URLs to copy");
		sb.append("\nSCALE <WIDTH> <HEIGHT> - Scale copied images to this size");

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
			Queue<String> argsQueue = new LinkedList<String>();
			argsQueue.addAll(Arrays.asList(args));

			String operation = argsQueue.poll();

			if (operation == null) {
				return false;
			}

			if (!(sender instanceof Player)) {
				return false;
			}

			Player player = (Player) sender;
			PlayerSettingsManager playerSettings = plugin.getConfigurationManager().getPlayerSettings(player.getName());

			if (!player.hasPermission(getPermissionNode())) {
				player.sendMessage("You do not have permission: " + getPermissionNode());
				return true;
			}

			if ("ON".equalsIgnoreCase(operation)) {
				playerSettings.setCopyEnabled(true);
				player.sendMessage("Copying has been enabled for " + player.getName());
			} else if ("OFF".equalsIgnoreCase(operation)) {
				playerSettings.setCopyEnabled(false);
				player.sendMessage("Copying has been disabled for " + player.getName());
			} else if ("SELECT".equalsIgnoreCase(operation)) {
				playerSettings.setActiveShortcut(argsQueue.poll());
			} else if ("TRIGGER".equalsIgnoreCase(operation)) {
				ItemStack itemInHand = player.getItemInHand();
				log.debug("Hand contents: " + itemInHand.getClass().getName() + " " + itemInHand.toString());
				playerSettings.setTrigger(itemInHand.toString());
			} else if ("SCALE".equalsIgnoreCase(operation)) {
				String width = argsQueue.poll();
				String height = argsQueue.poll();

				if (width == null) {
					player.sendMessage("Width not specified");
				} else if (height == null) {
					player.sendMessage("Height not specified");
				}

				int widthInt = Integer.parseInt(width);
				int heightInt = Integer.parseInt(height);

				playerSettings.setBuildDimensions(widthInt, heightInt);
			}

			return true;
		} catch (Exception e) {
			log.error("Something unexpected happened", e);
			sender.sendMessage(e.getMessage());
		}

		return false;
	}
}
