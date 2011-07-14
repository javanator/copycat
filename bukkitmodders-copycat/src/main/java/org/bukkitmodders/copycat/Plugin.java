package org.bukkitmodders.copycat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.io.IOUtils;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginDescriptionFile;
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
import org.yaml.snakeyaml.Yaml;

public class Plugin extends JavaPlugin {
	// http://javadoc.lukegb.com/BukkitJD/index.html

	private static final String DATAFILE = "pluginSettings.xml";
	private static final Logger log = LoggerFactory.getLogger(Plugin.class);
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

		@SuppressWarnings("unchecked")
		Map<String, Object> commands = (Map<String, Object>) pdfFile.getCommands();

		return commands.keySet().iterator().next().toString();
	}

	public HashMap<String, Stack<Stack<RevertableBlock>>> getUndoBuffers() {

		return undoBuffers;
	}

	public Map<String, AbstractCopycatFunction> getFunctions() {
		return functions;
	}

	public void outputYaml(String fileName, String pluginName, String pluginVersion) throws FileNotFoundException,
			IOException {

		StringWriter writer = new StringWriter();

		Yaml yaml = new Yaml();
		yaml.dump(getYamlMap(pluginName, pluginVersion), writer);

		log.info("Generated Yaml:\n" + writer.toString());

		if (fileName != null) {

			File file = new File(fileName);
			file.delete();

			IOUtils.copy(new StringReader(writer.toString()), new FileOutputStream(file));
		}
	}

	private Map<String, Object> getYamlMap(String pluginName, String pluginVersion) {

		Map<String, Object> yamlData = new HashMap<String, Object>();

		yamlData.put("name", pluginName);
		yamlData.put("main", Plugin.class.getName());
		yamlData.put("version", pluginVersion);

		Map<String, Object> commands = new HashMap<String, Object>();
		commands.put(Settings.DEFAULT_COMMAND_TRIGGER, CopycatCommand.getUsageDescMap());

		yamlData.put("commands", commands);

		return yamlData;
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {

		String yamlFileName = args[0];
		String pluginName = args[1];
		String pluginVersion = args[2];

		new Plugin().outputYaml(yamlFileName, pluginName, pluginVersion);
	}
}