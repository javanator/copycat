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
import org.bukkitmodders.copycat.managers.ConfigurationManager;
import org.bukkitmodders.copycat.managers.PlayerSettingsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WandCommand implements CommandExecutor {
	static final Logger log = LoggerFactory.getLogger(WandCommand.class);
	final Nouveau plugin;

	public WandCommand(Nouveau nouveau) {
		this.plugin = nouveau;
	}

	public static String getCommandString() {
		return "ccwand";
	}

	/**
	 * Used during build time to generate the plugin.yml file. Not used during
	 * runtime.
	 * 
	 * @return
	 */
	public static Map<String, Object> getDescription() {

		StringBuffer sb = new StringBuffer();
		sb.append("/" + getCommandString() + " [ ON | OFF | SET ]");
		sb.append("\nON - Enables image copy when the trigger is in the player's hand");
		sb.append("\nOFF - Disables image copy when the trigger is in the player's hand ");
		sb.append("\nSET - Sets the wand item. Defaults to empty fist. ");
		sb.append("will render an image selected by the " + SetCommand.getCommandString() + " command on item use (LEFT CLICK)");

		Map<String, Object> desc = new LinkedHashMap<String, Object>();
		desc.put("description", "Magic wand mode commands");
		desc.put("usage", sb.toString());

		return desc;
	}

	public static Map<String, Object> getPermissions() {

		Map<String, Object> permissions = new LinkedHashMap<String, Object>();
		permissions.put("description", "Renders images in-game via an equipped item. Must already have permission.build");
		permissions.put("default", "op");

		return permissions;
	}

	public static String getPermissionNode() {
		return "copycat.wand";
	}

	@Override
	public boolean onCommand(final CommandSender sender, Command command, String alias, String[] args) {
		try {
			Queue<String> argsQueue = new LinkedList<String>();
			argsQueue.addAll(Arrays.asList(args));
			String operation = argsQueue.poll();

			if (operation == null) {
				return false;
			}

			ConfigurationManager configurationManager = plugin.getConfigurationManager();
			PlayerSettingsManager playerSettings = configurationManager.getPlayerSettings(sender.getName());

			if ("ON".equalsIgnoreCase(operation)) {

				playerSettings.setCopyEnabled(true);
				sender.sendMessage("Copying has been enabled for " + sender.getName());
			} else if ("OFF".equalsIgnoreCase(operation)) {

				playerSettings.setCopyEnabled(false);
				sender.sendMessage("Copying has been disabled for " + sender.getName());
			} else if ("SET".equalsIgnoreCase(operation)) {

				Player player = (Player) sender;
				String itemTrigger = player.getItemInHand().getType().name();
				playerSettings.setTrigger(itemTrigger);

				sender.sendMessage("Using: " + itemTrigger + " will trigger rendering when ON");
			}

			return true;
		} catch (Exception e) {
			log.error("Something Unexpected Happened", e);
		}

		return false;
	}
}