package org.bukkitmodders.copycat;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingDeque;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkitmodders.copycat.functions.AbstractCopycatFunction;
import org.bukkitmodders.copycat.functions.HelpFunction;
import org.bukkitmodders.copycat.functions.OpFunctions;
import org.bukkitmodders.copycat.functions.SetFunction;
import org.bukkitmodders.copycat.functions.ShortcutFunctions;
import org.bukkitmodders.copycat.functions.UndoFunction;
import org.bukkitmodders.copycat.managers.ConfigurationManager;
import org.bukkitmodders.copycat.plugin.CopycatCommand;
import org.bukkitmodders.copycat.plugin.RevertableBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Plugin extends JavaPlugin {
	// http://javadoc.lukegb.com/BukkitJD/index.html

	private static final String DATAFILE = "pluginSettings.xml";
	private static final Logger log = LoggerFactory.getLogger(Plugin.class);
	private final HashMap<String, LinkedBlockingDeque<Stack<RevertableBlock>>> undoBuffers = new HashMap<String, LinkedBlockingDeque<Stack<RevertableBlock>>>();
	private ConfigurationManager configurationManager;
	private final Map<String, AbstractCopycatFunction> functions = new HashMap<String, AbstractCopycatFunction>();

	public Plugin() {

		AbstractCopycatFunction function = null;

		function = new HelpFunction(this);
		functions.put(function.getFunction(), function);

		function = new OpFunctions(this);
		functions.put(function.getFunction(), function);

		function = new ShortcutFunctions(this);
		functions.put(function.getFunction(), function);

		function = new UndoFunction(this);
		functions.put(function.getFunction(), function);

		function = new SetFunction(this);
		functions.put(function.getFunction(), function);
	}

	public void onDisable() {

		log.info("Plugin Disabled");
	}

	public void onEnable() {

		CopycatCommand ccCommand = new CopycatCommand(this);

		PluginCommand command = getCommand(ccCommand.getCommandString());
		command.setExecutor(ccCommand);

		log.info(getDescription().getName() + " " + getDescription().getVersion() + " Enabled");
	}

	public ConfigurationManager getConfigurationManager() {

		if (this.configurationManager == null) {

			String file = getDataFolder().getAbsolutePath() + File.separatorChar + DATAFILE;

			this.configurationManager = new ConfigurationManager(file);
		}

		return this.configurationManager;
	}

	public String getTriggerString() {
		PluginDescriptionFile pdfFile = getDescription();

		Map<String, Map<String, Object>> commands = pdfFile.getCommands();

		return commands.keySet().iterator().next().toString();
	}

	public HashMap<String, LinkedBlockingDeque<Stack<RevertableBlock>>> getUndoBuffers() {

		return undoBuffers;
	}

	public Map<String, AbstractCopycatFunction> getFunctions() {
		return functions;
	}
}