package org.bukkitmodders.copycat.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.bukkitmodders.copycat.Nouveau;
import org.bukkitmodders.copycat.WandListener;
import org.bukkitmodders.copycat.commands.AdminCommand;
import org.bukkitmodders.copycat.commands.CCCommand;
import org.bukkitmodders.copycat.commands.ImgCommand;
import org.bukkitmodders.copycat.commands.SetCommand;
import org.bukkitmodders.copycat.commands.UndoCommand;
import org.bukkitmodders.copycat.commands.WandCommand;
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

		Map<String, Object> commands = new LinkedHashMap<String, Object>();
		commands.put(ImgCommand.getCommandString(), ImgCommand.getDescription());
		commands.put(SetCommand.getCommandString(), SetCommand.getDescription());
		commands.put(UndoCommand.getCommandString(), UndoCommand.getDescription());
		commands.put(AdminCommand.getCommandString(), AdminCommand.getDescription());
		commands.put(CCCommand.getCommandString(), CCCommand.getDescription());
		commands.put(WandCommand.getCommandString(), WandCommand.getDescription());

		yamlData.put("commands", commands);

		Map<String, Object> permissions = new LinkedHashMap<String, Object>();
		permissions.put(ImgCommand.getPermissionNode(), ImgCommand.getPermissions());
		permissions.put(SetCommand.getPermissionNode(), SetCommand.getPermissions());
		permissions.put(UndoCommand.getPermissionNode(), UndoCommand.getPermissions());
		permissions.put(AdminCommand.getPermissionNode(), AdminCommand.getPermissions());
		permissions.put(CCCommand.getPermissionNode(), CCCommand.getPermissions());
		permissions.put(WandCommand.getPermissionNode(), WandCommand.getPermissions());

		yamlData.put("permissions", permissions);

		return yamlData;
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {

		String yamlFileName = args[0];
		String pluginName = args[1];
		String pluginVersion = args[2];

		new PluginDescriptorEmitter().outputYaml(yamlFileName, pluginName, pluginVersion);
	}
}
