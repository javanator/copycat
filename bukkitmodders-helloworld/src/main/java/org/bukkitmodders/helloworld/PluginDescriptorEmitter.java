package org.bukkitmodders.helloworld;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

public class PluginDescriptorEmitter {

	private static final Logger log = LoggerFactory.getLogger(PluginDescriptorEmitter.class);

	public static void main(String[] args) throws FileNotFoundException, IOException {

		String yamlFileName = args[0];
		String pluginName = args[1];
		String pluginVersion = args[2];

		new PluginDescriptorEmitter().outputYaml(yamlFileName, pluginName, pluginVersion);
	}

	private Map<String, Object> getYamlMap(String pluginName, String pluginVersion) {

		Map<String, Object> yamlData = new HashMap<String, Object>();

		yamlData.put("name", pluginName);
		yamlData.put("main", MyPlugin.class.getName());
		yamlData.put("version", pluginVersion);

		Map<String, Object> commands = new HashMap<String, Object>();
		commands.put(Settings.DEFAULT_COMMAND_TRIGGER, getUsageDescMap());

		yamlData.put("commands", commands);

		return yamlData;
	}

	private Object getUsageDescMap() {
		HashMap<String, Object> usage = new HashMap<String, Object>();
		usage.put("description", "this is a description of my plugin");
		usage.put("usage", "this is usage help for my plugin");

		return usage;
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
}
