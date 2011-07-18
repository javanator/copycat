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
import org.bukkitmodders.copycat.Settings;
import org.bukkitmodders.copycat.schema.BlockProfileType;
import org.bukkitmodders.copycat.schema.ObjectFactory;
import org.bukkitmodders.copycat.schema.PlayerSettingsType;
import org.bukkitmodders.copycat.schema.PluginConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationManager {

	private Logger log = LoggerFactory.getLogger(ConfigurationManager.class);

	private final ObjectFactory of = new ObjectFactory();
	private final String file;

	public ConfigurationManager(String file) {

		this.file = file;
	}

	public PlayerSettingsManager getPlayerSettings(String targetPlayerName) {

		PluginConfig pluginConfig = getPluginConfig();
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
		}

		return new PlayerSettingsManager(toReturn, this);
	}

	private PlayerSettingsType createDefaultPlayerSettings(String playerName) {

		PlayerSettingsType playerSettings = of.createPlayerSettingsType();
		playerSettings.setPlayerName(playerName);
		playerSettings.setPlayerEnabled(true);
		playerSettings.setShortcuts(of.createPlayerSettingsTypeShortcuts());
		playerSettings.setBlockProfile("default");
		playerSettings.setBuildWidth(getPluginConfig().getGlobalSettings().getMaxImageWidth());
		playerSettings.setBuildHeight(getPluginConfig().getGlobalSettings().getMaxImageHeight());

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

		for (BlockProfileType blockProfile : getPluginConfig().getGlobalSettings().getBlockProfiles().getBlockProfile()) {
			profiles.put(blockProfile.getName(), blockProfile);
		}

		return profiles;
	}

	private synchronized void persist(PluginConfig pluginConfig) {

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

	public synchronized void savePlayerSettings(PlayerSettingsType playerSettings) {

		PluginConfig pluginConfig = getPluginConfig();

		List<PlayerSettingsType> playerPreferences = pluginConfig.getPreferences().getPlayerPreferences();

		for (int i = 0; i < playerPreferences.size(); i++) {

			PlayerSettingsType currentPlayerSettings = playerPreferences.get(i);

			if (currentPlayerSettings.getPlayerName().equalsIgnoreCase(playerSettings.getPlayerName())) {
				playerPreferences.remove(i);
			}
		}

		playerPreferences.add(playerSettings);
		persist(pluginConfig);
	}

	private synchronized File getDataFile() {

		File dataFile = new File(file);
		dataFile.getParentFile().mkdirs();

		try {
			if (dataFile.createNewFile() || dataFile.length() == 0) {
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
			in = getClass().getResourceAsStream(Settings.DEFAULT_SETTINGS_XML);
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

		List<String> worlds = getPluginConfig().getGlobalSettings().getProhibitedWorlds().getWorld();

		if (worlds.contains(name)) {
			return false;
		}

		return true;
	}

	public void enableWorld(String name) {

		PluginConfig pluginConfig = getPluginConfig();
		List<String> worlds = pluginConfig.getGlobalSettings().getProhibitedWorlds().getWorld();

		worlds.remove(name);

		persist(pluginConfig);
	}

	public void disableWorld(String name) {
		PluginConfig pluginConfig = getPluginConfig();
		List<String> worlds = pluginConfig.getGlobalSettings().getProhibitedWorlds().getWorld();

		if (!worlds.contains(name)) {
			worlds.add(name);
		}

		persist(pluginConfig);
	}

	public int getMaxImageWidth() {
		return getPluginConfig().getGlobalSettings().getMaxImageWidth();
	}

	public int getMaxImageHeight() {
		return getPluginConfig().getGlobalSettings().getMaxImageHeight();
	}

	private synchronized PluginConfig getPluginConfig() {

		PluginConfig pluginConfig = JAXB.unmarshal(getDataFile(), PluginConfig.class);

		return pluginConfig;
	}
}
