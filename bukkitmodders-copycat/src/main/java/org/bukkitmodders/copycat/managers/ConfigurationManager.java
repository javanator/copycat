package org.bukkitmodders.copycat.managers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXB;

import org.apache.commons.io.IOUtils;
import org.bukkitmodders.copycat.schema.BlockProfileType;
import org.bukkitmodders.copycat.schema.ObjectFactory;
import org.bukkitmodders.copycat.schema.PlayerSettingsType;
import org.bukkitmodders.copycat.schema.PluginConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationManager {

	private Logger log = LoggerFactory.getLogger(ConfigurationManager.class);

	private final File dataFile;
	private final PluginConfig pluginConfig;
	private final ObjectFactory of = new ObjectFactory();

	public ConfigurationManager(File dataFile) {

		this.dataFile = dataFile;

		PluginConfig pluginConfig = JAXB.unmarshal(getDataFile(), PluginConfig.class);
		this.pluginConfig = pluginConfig;

		log.info("Loaded Config File: " + getDataFile().getAbsolutePath());
	}

	public PlayerSettingsManager getPlayerSettings(String targetPlayerName) {

		List<PlayerSettingsType> playerPreferences = pluginConfig.getPreferences().getPlayerPreferences();

		PlayerSettingsType toReturn = null;

		for (PlayerSettingsType playerSettings : playerPreferences) {
			if (playerSettings.getPlayerName().equalsIgnoreCase(targetPlayerName)) {
				toReturn = playerSettings;
				break;
			}
		}

		if (toReturn == null) {
			// Player has no settings. Make a new one.
			toReturn = createDefaultPlayerSettings(targetPlayerName);
			playerPreferences.add(toReturn);

			persist();
		}

		return new PlayerSettingsManager(toReturn, this);
	}

	private PlayerSettingsType createDefaultPlayerSettings(String playerName) {

		PlayerSettingsType playerSettings = of.createPlayerSettingsType();
		playerSettings.setPlayerName(playerName);
		playerSettings.setPlayerEnabled(true);
		playerSettings.setShortcuts(of.createPlayerSettingsTypeShortcuts());
		playerSettings.setBlockProfile("default");
		playerSettings.setBuildWidth(pluginConfig.getGlobalSettings().getMaxImageWidth());
		playerSettings.setBuildHeight(pluginConfig.getGlobalSettings().getMaxImageHeight());

		return playerSettings;
	}

	public BlockProfileType getBlockProfile(String blockProfileName) {

		if (getBlockProfiles().containsKey(blockProfileName)) {
			return getBlockProfiles().get(blockProfileName);
		}

		log.warn("Block Profile Does Not Exist: " + blockProfileName + " Returning Default");

		return getBlockProfiles().get("default");
	}

	public Map<String, BlockProfileType> getBlockProfiles() {

		Map<String, BlockProfileType> profiles = new HashMap<String, BlockProfileType>();

		for (BlockProfileType blockProfile : pluginConfig.getGlobalSettings().getBlockProfiles().getBlockProfile()) {
			profiles.put(blockProfile.getName(), blockProfile);
		}

		return profiles;
	}

	public void save() {
		persist();
	}

	synchronized void persist() {

		OutputStream out = null;

		try {
			out = new FileOutputStream(getDataFile());
			JAXB.marshal(pluginConfig, out);
		} catch (FileNotFoundException e) {
			log.error("Error persisting config", e);
		} finally {
			IOUtils.closeQuietly(out);
		}

	}

	public File getDataFile() {

		dataFile.getParentFile().mkdirs();

		try {
			if (dataFile.createNewFile()) {
				createDefaultConfig(dataFile);
			}
		} catch (IOException e) {
			log.error("Error creating default data file", e);
		}

		return dataFile;
	}

	private void createDefaultConfig(File dataFile) {

		InputStream in = null;
		OutputStream out = null;

		try {
			in = getClass().getResourceAsStream("/defaultSettings.xml");
			out = new FileOutputStream(dataFile);

			IOUtils.copy(in, out);

		} catch (IOException e) {
			log.error("Error copying default configuration.", e);
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}
	}

	public boolean isWorldEnabled(String name) {

		List<String> worlds = pluginConfig.getGlobalSettings().getProhibitedWorlds().getWorld();

		if (worlds.contains(name)) {
			return false;
		}

		return true;
	}

	public void enableWorld(String name) {

		List<String> worlds = pluginConfig.getGlobalSettings().getProhibitedWorlds().getWorld();

		worlds.remove(name);

		persist();
	}

	public void disableWorld(String name) {
		List<String> worlds = pluginConfig.getGlobalSettings().getProhibitedWorlds().getWorld();

		if (!worlds.contains(name)) {
			worlds.add(name);
		}

		persist();
	}

	public int getMaxImageWidth() {
		return pluginConfig.getGlobalSettings().getMaxImageWidth();
	}

	public int getMaxImageHeight() {
		return pluginConfig.getGlobalSettings().getMaxImageHeight();
	}
}