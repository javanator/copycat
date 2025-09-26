package org.bukkitmodders.copycat.managers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkitmodders.copycat.Settings;
import org.bukkitmodders.copycat.model.BlockProfileType;
import org.bukkitmodders.copycat.model.BlockProfileType.Block;
import org.bukkitmodders.copycat.model.ObjectFactory;
import org.bukkitmodders.copycat.model.PlayerSettingsType;
import org.bukkitmodders.copycat.model.PluginConfig;
import org.bukkitmodders.copycat.services.TextureMappedBlock;
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

		if (toReturn == null && !StringUtils.isBlank(targetPlayerName)) {
			// Player has no settings. Make a new one.
			toReturn = createDefaultPlayerSettings(targetPlayerName);
		}

		return new PlayerSettingsManager(toReturn, this);
	}

	private PlayerSettingsType createDefaultPlayerSettings(String playerName) {

		PlayerSettingsType playerSettings = of.createPlayerSettingsType();
		playerSettings.setPlayerName(playerName);
		playerSettings.setStampActivated(false);
		playerSettings.setShortcuts(of.createPlayerSettingsTypeShortcuts());
		playerSettings.setBlockProfile("default");
		playerSettings.setActiveShortcut("");
		playerSettings.setStampItem("");
		playerSettings.setBuildWidth(200);
		playerSettings.setBuildHeight(200);
		playerSettings.setDithering(true);
		playerSettings.setUndoEnabled(true);

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
            new ObjectMapper().writeValue(out,pluginConfig);
		} catch (Exception e) {
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
			in = getClass().getResourceAsStream(Settings.DEFAULT_SETTINGS_JSON);
			out = new FileOutputStream(dataFile);

			IOUtils.copy(in, out);

		} catch (IOException e) {
			log.error("Error copying default configuration.", e);
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}
	}

	public int getMaxImageWidth() {
		return getPluginConfig().getGlobalSettings().getMaxImageWidth();
	}

	public int getMaxImageHeight() {
		return getPluginConfig().getGlobalSettings().getMaxImageHeight();
	}

	private synchronized PluginConfig getPluginConfig() {


        PluginConfig pluginConfig = null;
        try {
            pluginConfig = new ObjectMapper().readValue(getDataFile(), PluginConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return pluginConfig;
	}

	public static BlockProfileType generateDefaultBlockProfile() {

		BlockProfileType newBlockProfile = new BlockProfileType();
		newBlockProfile.setName("default");

		for (TextureMappedBlock block : TextureMappedBlock.values()) {
			Block b = new Block();
			b.setName(block.getName());
			b.setTextureIndex(block.getTile());
			newBlockProfile.getBlock().add(b);
		}

		return newBlockProfile;
	}

	public void updateDefaultBlockProfile(BlockProfileType blockProfile) {
		PluginConfig pluginConfig = getPluginConfig();
		List<BlockProfileType> blockProfiles = pluginConfig.getGlobalSettings().getBlockProfiles().getBlockProfile();

		Iterator<BlockProfileType> itr = blockProfiles.iterator();
		while (itr.hasNext()) {
			BlockProfileType bpt = itr.next();
			if ("default".equalsIgnoreCase(bpt.getName())) {
				itr.remove();
			}
		}

		blockProfile.setName("default");
		blockProfiles.add(blockProfile);
		persist(pluginConfig);

	}
}
