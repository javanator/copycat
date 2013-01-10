package org.bukkitmodders.copycat;

import java.io.File;
import java.util.HashMap;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingDeque;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkitmodders.copycat.commands.ImgCommand;
import org.bukkitmodders.copycat.commands.SetCommand;
import org.bukkitmodders.copycat.commands.UndoCommand;
import org.bukkitmodders.copycat.managers.ConfigurationManager;
import org.bukkitmodders.copycat.plugin.RevertableBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Nouveau extends JavaPlugin {

	private static Logger log = LoggerFactory.getLogger(Nouveau.class);
	private static final String DATAFILE = "pluginSettings.xml";
	private final HashMap<String, LinkedBlockingDeque<Stack<RevertableBlock>>> undoBuffers = new HashMap<String, LinkedBlockingDeque<Stack<RevertableBlock>>>();
	private ConfigurationManager configurationManager;

	public void onDisable() {

		getCommand(ImgCommand.getCommandString()).setExecutor(null);
		getCommand(SetCommand.getCommandString()).setExecutor(null);
		getCommand(UndoCommand.getCommandString()).setExecutor(null);

		log.info(getDescription().getName() + " " + getDescription().getVersion() + " Disabled");
	}

	public void onEnable() {

		// Throws null if not found in the YAML
		getCommand(ImgCommand.getCommandString()).setExecutor(new ImgCommand(this));
		getCommand(SetCommand.getCommandString()).setExecutor(new SetCommand(this));
		getCommand(UndoCommand.getCommandString()).setExecutor(new UndoCommand(this));

		getServer().getPluginManager().registerEvents(new CopycatPlayerListener(this), this);

		log.info(getDescription().getName() + " " + getDescription().getVersion() + " Enabled");
	}

	public ConfigurationManager getConfigurationManager() {

		if (this.configurationManager == null) {

			String file = getDataFolder().getAbsolutePath() + File.separatorChar + DATAFILE;

			this.configurationManager = new ConfigurationManager(file);
		}

		return this.configurationManager;
	}

	public HashMap<String, LinkedBlockingDeque<Stack<RevertableBlock>>> getUndoBuffers() {

		return undoBuffers;
	}
}
