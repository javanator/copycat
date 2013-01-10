package org.bukkitmodders.copycat;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetCommand implements CommandExecutor {

	private static Logger log = LoggerFactory.getLogger(SetCommand.class);

	public static String getCommandString() {
		return "ccset";
	}

	public static Map<String, Object> getDescription() {
		StringBuffer sb =  new StringBuffer();
		sb.append("/"+getCommandString() + " [ ON | OFF | SELECT | TRIGGER ]");
		sb.append("\nON|OFF - Enables or disables image copy when the trigger is in the player's hand");
		sb.append("\nTRIGGER - Sets the activation item. Defaults to fist");
		sb.append("\nSELECT - Pick an image from the list of URLs to copy");
		sb.append("\nDIM - Sets the maximum build dimensions a copied image will scale to");
		
		
		Map<String, Object> desc = new LinkedHashMap<String, Object>();
		desc.put("description", "Sets plugin properties");
		desc.put("usage", sb.toString());

		return desc;
	}

	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
		try {
			
			if (arg0 instanceof Player) {
				Player player = (Player) arg0;
				ItemStack itemInHand = player.getItemInHand();

				log.debug("Hand contents: " + itemInHand.getClass().getName() + " " + itemInHand.toString());
			}

			return true;
		} catch (Exception e) {

		}

		return false;
	}
}
