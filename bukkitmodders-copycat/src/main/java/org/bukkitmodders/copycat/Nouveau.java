package org.bukkitmodders.copycat;

import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Nouveau extends JavaPlugin {

	private static Logger log = LoggerFactory.getLogger(Nouveau.class);

	public void onDisable() {

		getCommand(ImgCommand.getCommandString()).setExecutor(null);
		getCommand(SetCommand.getCommandString()).setExecutor(null);
		getCommand(UndoCommand.getCommandString()).setExecutor(null);

		log.info("Plugin Disabled");
	}

	public void onEnable() {
		
		// Throws null if not found in the YAML
		getCommand(ImgCommand.getCommandString()).setExecutor(new ImgCommand());
		getCommand(SetCommand.getCommandString()).setExecutor(new SetCommand());
		getCommand(UndoCommand.getCommandString()).setExecutor(new UndoCommand());

		log.info(getDescription().getName() + " " + getDescription().getVersion() + " Enabled");
	}

}
