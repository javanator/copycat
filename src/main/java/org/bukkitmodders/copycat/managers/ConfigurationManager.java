package org.bukkitmodders.copycat.managers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.bukkitmodders.copycat.Application;
import org.bukkitmodders.copycat.Settings;
import org.bukkitmodders.copycat.model.BlockProfileType;
import org.bukkitmodders.copycat.model.BlockProfileType.Block;
import org.bukkitmodders.copycat.model.PlayerSettingsType;
import org.bukkitmodders.copycat.model.PlayerSettingsType.Shortcut;
import org.bukkitmodders.copycat.model.PluginConfig;
import org.bukkitmodders.copycat.services.TextureMappedBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigurationManager {

    private final Logger log = LoggerFactory.getLogger(ConfigurationManager.class);

    private final String file;
    private final Application application;

    public ConfigurationManager(String file, Application application) {
        this.file = file;
        this.application = application;
    }

    public PlayerSettingsManager getPlayerSettings(String targetPlayerName) {
        PluginConfig pluginConfig = getPluginConfig();
        PlayerSettingsType playerSettingsOptional = pluginConfig.getPreferences().getPlayerPreferences().stream()
                .filter(playerSettingsType ->
                        playerSettingsType.getPlayerName().equalsIgnoreCase(targetPlayerName))
                .findFirst()
                .orElseGet(() -> createDefaultPlayerSettings(targetPlayerName));
        return new PlayerSettingsManager(playerSettingsOptional, application);
    }

    public List<PlayerSettingsType> getAllPlayerSettings() {
        return getPluginConfig().getPreferences().getPlayerPreferences();
    }


    private PlayerSettingsType createDefaultPlayerSettings(String playerName) {
        PlayerSettingsType playerSettings = new PlayerSettingsType();
        playerSettings.setPlayerName(playerName);
        playerSettings.setStampActivated(false);
        playerSettings.setShortcuts(new ArrayList<Shortcut>());
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
        List<BlockProfileType> blockProfiles = getPluginConfig().getGlobalSettings().getBlockProfiles();
        Map<String, BlockProfileType> profileMap = new HashMap<>();
        blockProfiles.forEach(bp -> profileMap.put(bp.getName(), bp));
        return profileMap;
    }

    private synchronized void persist(PluginConfig pluginConfig) {
        OutputStream out = null;
        try {
            out = new FileOutputStream(getDataFile());
            new ObjectMapper().writeValue(out, pluginConfig);
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

    public int getMaxUndoSize() {return getPluginConfig().getGlobalSettings().getUndoBufferLimit();}

    private synchronized PluginConfig getPluginConfig() {
        PluginConfig pluginConfig = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            pluginConfig = mapper.readValue(getDataFile(), PluginConfig.class);
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
}
