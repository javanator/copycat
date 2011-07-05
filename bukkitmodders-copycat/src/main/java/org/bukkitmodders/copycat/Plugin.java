package org.bukkitmodders.copycat;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkitmodders.copycat.functions.AbstractCopycatFunction;
import org.bukkitmodders.copycat.functions.HelpFunction;
import org.bukkitmodders.copycat.functions.OpFunctions;
import org.bukkitmodders.copycat.functions.SetFunction;
import org.bukkitmodders.copycat.functions.ShortcutFunctions;
import org.bukkitmodders.copycat.functions.UndoFunction;
import org.bukkitmodders.copycat.managers.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Plugin extends JavaPlugin {
	// http://javadoc.lukegb.com/BukkitJD/index.html

	private static final String DATAFILE = "pluginSettings.xml";
	private final Logger log = LoggerFactory.getLogger(Plugin.class);
	private final HashMap<String, Stack<Stack<RevertableBlock>>> undoBuffers = new HashMap<String, Stack<Stack<RevertableBlock>>>();
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

		getConfigurationManager().save();

		log.info("Plugin Disabled");
	}

	public void onEnable() {

		PluginManager pm = getServer().getPluginManager();

		CopycatCommand ccCommand = new CopycatCommand(this);
		PluginCommand command = getCommand(ccCommand.getCommandString());
		command.setExecutor(ccCommand);

		log.info(getDescription().getName() + " " + getDescription().getVersion() + " Enabled");
	}

	public void reloadConfig() {
		this.configurationManager = null;
	}

	public ConfigurationManager getConfigurationManager() {

		if (this.configurationManager == null) {

			File dataFile = new File(getDataFolder().getAbsolutePath() + File.separatorChar + DATAFILE);

			this.configurationManager = new ConfigurationManager(dataFile);
		}

		return this.configurationManager;
	}

	public HashMap<String, Stack<Stack<RevertableBlock>>> getUndoBuffers() {

		return undoBuffers;
	}

	public Map<String, AbstractCopycatFunction> getFunctions() {
		return functions;
	}

	public static void main(String[] args) {
		// Some Bukkit Genius had a brilliant idea to make YML files which
		// make easy automated builds a pain in the ass.
		StringBuffer ymlCmds = new StringBuffer();
		ymlCmds.append("commands:\n");
		String indent = " ";
		// new AdminCommand(null).appendYml(indent, ymlCmds);
		new CopycatCommand(null).appendYml(indent, ymlCmds);
		System.out.println(ymlCmds.toString());
	}
}