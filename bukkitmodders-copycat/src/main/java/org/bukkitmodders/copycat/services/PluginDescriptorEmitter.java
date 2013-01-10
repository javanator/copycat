package org.bukkitmodders.copycat.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.bukkitmodders.copycat.AdminCommand;
import org.bukkitmodders.copycat.ImgCommand;
import org.bukkitmodders.copycat.Nouveau;
import org.bukkitmodders.copycat.SetCommand;
import org.bukkitmodders.copycat.UndoCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

public class PluginDescriptorEmitter {

	private static final Logger log = LoggerFactory.getLogger(PluginDescriptorEmitter.class);

	private void outputYaml(String fileName, String pluginName, String pluginVersion) throws FileNotFoundException, IOException {

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

		Map<String, Object> yamlData = new LinkedHashMap<String, Object>();

		yamlData.put("name", pluginName);
		yamlData.put("main", Nouveau.class.getName());
		yamlData.put("version", pluginVersion);

		Map<String, Object> commands = new HashMap<String, Object>();
		commands.put(ImgCommand.getCommandString(), ImgCommand.getDescription());
		commands.put(SetCommand.getCommandString(), SetCommand.getDescription());
		commands.put(UndoCommand.getCommandString(), UndoCommand.getDescription());
		commands.put(AdminCommand.getCommandString(), AdminCommand.getDescription());

		yamlData.put("commands", commands);

		return yamlData;
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {

		String yamlFileName = args[0];
		String pluginName = args[1];
		String pluginVersion = args[2];

		new PluginDescriptorEmitter().outputYaml(yamlFileName, pluginName, pluginVersion);
	}
}
