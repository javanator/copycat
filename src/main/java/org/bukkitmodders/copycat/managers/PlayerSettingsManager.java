package org.bukkitmodders.copycat.managers;

import org.bukkitmodders.copycat.Application;
import org.bukkitmodders.copycat.model.PlayerSettingsType;
import org.bukkitmodders.copycat.model.PlayerSettingsType.Shortcut;
import org.bukkitmodders.copycat.model.RevertibleBlock;
import org.bukkitmodders.copycat.model.UndoHistoryComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.List;
import java.util.Stack;

/**
 * Manages player-specific settings and preferences for the Copycat plugin.
 * This class handles operations related to player shortcuts, build dimensions,
 * undo buffers, block profiles, and various stamp-related settings.
 * It provides methods to get/set player preferences and maintains persistence
 * of these settings through the ConfigurationManager.
 */
public class PlayerSettingsManager {

    private final PlayerSettingsType playerSettings;
    private final Logger log = LoggerFactory.getLogger(PlayerSettingsManager.class);
    private final Application application;

    public PlayerSettingsManager(final PlayerSettingsType playerSettings, Application application) {
        this.playerSettings = playerSettings;
        this.application = application;
    }

    public void addShortcut(final String name, final String url) {
        final List<Shortcut> shortcuts = ensureShortcutsList();
        Shortcut shortcut = findShortcutByName(name);
        if (shortcut == null) {
            shortcut = new Shortcut();
            shortcuts.add(shortcut);
        }
        shortcut.setName(name);
        shortcut.setUrl(url);
        saveSettings();
    }

    public void deleteShortcut(final String name) {
        final Shortcut shortcut = findShortcutByName(name);
        if (shortcut != null) {
            ensureShortcutsList().remove(shortcut);
            saveSettings();
        }
    }

    public List<Shortcut> getShortcuts() {
        return playerSettings.getShortcuts();
    }

    public String getBlockProfile() {
        return playerSettings.getBlockProfile();
    }

    public void setBlockProfile(final String blockProfileName) {
        playerSettings.setBlockProfile(blockProfileName);
        saveSettings();
    }

    public void setBuildDimensions(final int width, final int height) {
        playerSettings.setBuildWidth(width);
        playerSettings.setBuildHeight(height);
        saveSettings();
    }

    public int getMaxBuildHeight() {
        return Math.min(application.getConfigurationManager().getMaxImageHeight(), playerSettings.getBuildHeight());
    }

    public int getMaxBuildWidth() {
        return Math.min(application.getConfigurationManager().getMaxImageWidth(), playerSettings.getBuildWidth());
    }

    public boolean isDithering() {
        return playerSettings.isDithering();
    }

    public void setDithering(final boolean value) {
        playerSettings.setDithering(value);
        saveSettings();
    }

    public Deque<UndoHistoryComponent> getUndoBuffer() {
        return application.getUndoBufferManager().getUndoBuffer(playerSettings.getPlayerName());
    }

    private void saveSettings() {
        application.getConfigurationManager().savePlayerSettings(playerSettings);
    }

    private List<Shortcut> ensureShortcutsList() {
        if (playerSettings.getShortcuts() == null) {
            playerSettings.setShortcuts(new java.util.ArrayList<>());
        }
        return playerSettings.getShortcuts();
    }

    private Shortcut findShortcutByName(final String name) {
        if (name == null || playerSettings.getShortcuts() == null) return null;
        for (Shortcut s : playerSettings.getShortcuts()) {
            if (name.equalsIgnoreCase(s.getName())) {
                return s;
            }
        }
        return null;
    }

}
