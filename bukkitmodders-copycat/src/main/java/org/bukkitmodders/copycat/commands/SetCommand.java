package org.bukkitmodders.copycat.commands;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkitmodders.copycat.Nouveau;
import org.bukkitmodders.copycat.plugin.NeedMoreArgumentsException;
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
		sb.append("/" + getCommandString() + " [ ON | OFF | SELECT | TRIGGER ]");
		sb.append("\nON - Enables image copy when the trigger is in the player's hand");
		sb.append("\nOFF - Disables image copy when the trigger is in the player's hand");
		sb.append("\nTRIGGER - Sets the activation item. Defaults to fist");
		sb.append("\nSELECT <imagename>- Pick an image from the list of URLs to copy");
		sb.append("\nDIM <X> <Y> - Scale copied images to this size");

		Map<String, Object> desc = new LinkedHashMap<String, Object>();
		desc.put("description", "Sets plugin properties");
		desc.put("usage", sb.toString());

		return desc;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		try {

			if (sender instanceof Player) {
				Player player = (Player) sender;
				ItemStack itemInHand = player.getItemInHand();

				log.debug("Hand contents: " + itemInHand.getClass().getName() + " " + itemInHand.toString());
				log.debug("arg2: " + alias + " arg3 " + Arrays.toString(args));
			}

			return true;
//		} catch (NeedMoreArgumentsException e) {
			
		} catch (Exception e) {

		}

		return false;
	}
}
