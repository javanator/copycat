package org.bukkitmodders.copycat;

import java.io.File;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkitmodders.copycat.commands.AdminCommand;
import org.bukkitmodders.copycat.commands.CCCommand;
import org.bukkitmodders.copycat.commands.ImgCommand;
import org.bukkitmodders.copycat.commands.SetCommand;
import org.bukkitmodders.copycat.commands.UndoCommand;
import org.bukkitmodders.copycat.commands.WandCommand;
import org.bukkitmodders.copycat.managers.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Nouveau extends JavaPlugin {

	private static Logger log = LoggerFactory.getLogger(Nouveau.class);
	private static final String DATAFILE = "pluginSettings.xml";
	private ConfigurationManager configurationManager;

	public void onDisable() {

		getCommand(ImgCommand.getCommandString()).setExecutor(null);
		getCommand(SetCommand.getCommandString()).setExecutor(null);
		getCommand(UndoCommand.getCommandString()).setExecutor(null);
		getCommand(AdminCommand.getCommandString()).setExecutor(null);
		getCommand(WandCommand.getCommandString()).setExecutor(null);
		getCommand(CCCommand.getCommandString()).setExecutor(null);

		log.info(getDescription().getName() + " " + getDescription().getVersion() + " Disabled");
	}

	public void onEnable() {

		// Throws null if not found in the YAML
		getCommand(ImgCommand.getCommandString()).setExecutor(new ImgCommand(this));
		getCommand(SetCommand.getCommandString()).setExecutor(new SetCommand(this));
		getCommand(UndoCommand.getCommandString()).setExecutor(new UndoCommand(this));
		getCommand(AdminCommand.getCommandString()).setExecutor(new AdminCommand(this));
		getCommand(WandCommand.getCommandString()).setExecutor(new WandCommand(this));
		getCommand(CCCommand.getCommandString()).setExecutor(new CCCommand(this));

		getServer().getPluginManager().registerEvents(new WandListener(this), this);

		log.info(getDescription().getName() + " " + getDescription().getVersion() + " Enabled");
	}

	public ConfigurationManager getConfigurationManager() {

		if (this.configurationManager == null) {

			String file = getDataFolder().getAbsolutePath() + File.separatorChar + DATAFILE;

			this.configurationManager = new ConfigurationManager(file);
		}

		return this.configurationManager;
	}
}
