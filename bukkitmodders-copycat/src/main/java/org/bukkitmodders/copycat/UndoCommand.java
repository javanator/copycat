package org.bukkitmodders.copycat;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class UndoCommand implements CommandExecutor {

	public static String getCommandString() {
		return "ccundo";
	}

	public static Map<String, Object> getDescription() {

		StringBuffer sb =  new StringBuffer();
		sb.append("/"+getCommandString() + " [ ON | OFF | <PLAYER> ]");
		sb.append("\n ON|OFF - Enables or disables the undo buffer. Off speeds larger images.");
		sb.append("\n <PLAYER> - For OPs Undoes another player's creation.");
		
		Map<String, Object> desc = new LinkedHashMap<String, Object>();
		desc.put("description", " Undoes an image copy.");
		desc.put("usage", sb.toString());

		return desc;
	}

	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
		// TODO Auto-generated method stub
		return false;
	}

}
