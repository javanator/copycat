package org.bukkitmodders.copycat;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ImgCommand implements CommandExecutor {

	public static String getCommandString() {
		return "ccimg";
	}

	public static Map<String, Object> getDescription() {
		
		StringBuffer sb =  new StringBuffer();
		sb.append("/"+getCommandString() + " [ ADD | DEL | CLEAN ]");
		sb.append("\nADD - Add an image URL. Ex: /"+getCommandString()+" ADD <NAME>=<URL>");
		sb.append("\nDEL - Deletes an image URL by name");
		sb.append("\nCLEAN - Automatically cleans invalid images");
		
		Map<String, Object> desc = new LinkedHashMap<String, Object>();
		desc.put("description","Commands for various image operations");
		desc.put("usage", sb.toString());

		return desc;
	}

	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
		// TODO Auto-generated method stub
		return false;
	}

}
