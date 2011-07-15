package org.bukkitmodders.helloworld;

import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;

public class NoopCommand implements CommandExecutor {

	private MyPlugin plugin;

	public NoopCommand(MyPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {

		// This command does nothing

		return false;
	}

	public String getTriggerString() {

		PluginDescriptionFile description = plugin.getDescription();

		@SuppressWarnings("unchecked")
		Map<String, Object> commands = (Map<String, Object>) description.getCommands();

		return commands.keySet().iterator().next().toString();
	}
}
