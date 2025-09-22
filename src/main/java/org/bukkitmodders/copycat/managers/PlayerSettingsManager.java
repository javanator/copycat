package org.bukkitmodders.copycat.managers;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingDeque;
import javax.imageio.ImageIO;

import org.bukkit.command.CommandSender;
import org.bukkitmodders.copycat.model.RevertibleBlock;
import org.bukkitmodders.copycat.model.PlayerSettingsType;
import org.bukkitmodders.copycat.model.PlayerSettingsType.Shortcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Manages player-specific settings and preferences for the Copycat plugin.
 * This class handles operations related to player shortcuts, build dimensions,
 * undo buffers, block profiles, and various stamp-related settings.
 * It provides methods to get/set player preferences and maintains persistence
 * of these settings through the ConfigurationManager.
 */
public class PlayerSettingsManager {
    private final PlayerSettingsType playerSettings;
    private final ConfigurationManager cm;
    private final Logger log = LoggerFactory.getLogger(PlayerSettingsManager.class);
    private final UndoBufferManager undoBufferManager;

    PlayerSettingsManager(PlayerSettingsType playerSettings, ConfigurationManager configurationManager) {
        this.playerSettings = playerSettings;
        this.cm = configurationManager;
        this.undoBufferManager = UndoBufferManager.getInstance();
    }

    public static void purgeAllUndoBuffers() {
        UndoBufferManager.getInstance().purgeAll();
    }

    public LinkedBlockingDeque<Stack<RevertibleBlock>> getUndoBuffer() {
        return undoBufferManager.getUndoBuffer(playerSettings.getPlayerName());
    }

    public Shortcut getShortcut(String name) {
        if (playerSettings.getShortcuts() == null) {
            return null;
        }
        for (Shortcut shortcut : playerSettings.getShortcuts()) {
            if (shortcut.getName().equalsIgnoreCase(name)) {
                return shortcut;
            }
        }
        return null;
    }

    public void addShortcut(String name, String url) {
        if (playerSettings.getShortcuts() == null) {
            playerSettings.setShortcuts(new java.util.ArrayList<>());
        }
        Shortcut shortcut = getShortcut(name);
        if (shortcut == null) {
            shortcut = new Shortcut();
            playerSettings.getShortcuts().add(shortcut);
        }
        shortcut.setName(name);
        shortcut.setUrl(url);
        saveSettings();
    }

    public void deleteShortcut(String name) {
        Shortcut shortcut = getShortcut(name);
        if (shortcut != null) {
            playerSettings.getShortcuts().remove(shortcut);
        }
        saveSettings();
    }

    public void tellShortcuts(CommandSender player) {
        List<Shortcut> shortcuts = playerSettings.getShortcuts();
        if (shortcuts == null || shortcuts.isEmpty()) {
            return;
        }
        for (Shortcut shortcut : shortcuts) {
            player.sendMessage(shortcut.getName() + "=" + shortcut.getUrl());
        }
    }

    public List<Shortcut> getShortcuts() {
        return playerSettings.getShortcuts();
    }

    public boolean isStampModeActivated() {
        return playerSettings.isStampActivated();
    }

    public String getBlockProfile() {
        return playerSettings.getBlockProfile();
    }

    public void setBlockProfile(String blockProfileName) {
        playerSettings.setBlockProfile(blockProfileName);
        saveSettings();
    }

    public void setBuildDimensions(int width, int height) {
        playerSettings.setBuildWidth(width);
        playerSettings.setBuildHeight(height);
        saveSettings();
    }

    public int getMaxBuildHeight() {
        return Math.min(cm.getMaxImageHeight(), playerSettings.getBuildHeight());
    }

    public int getMaxBuildWidth() {
        return Math.min(cm.getMaxImageWidth(), playerSettings.getBuildWidth());
    }

    public Shortcut getStampShortcut() {
        return getShortcut(playerSettings.getActiveShortcut());
    }

    public void cleanShortcuts() {
        List<Shortcut> shortcuts = playerSettings.getShortcuts();
        if (shortcuts == null) {
            return;
        }
        final Iterator<Shortcut> shortcutsIterator = shortcuts.iterator();
        while (shortcutsIterator.hasNext()) {
            Shortcut shortcut = shortcutsIterator.next();
            if (!isValidImageUrl(shortcut)) {
                shortcutsIterator.remove();
                log.info("URL is not an image or is invalid. Removed: {} {}", shortcut.getName(), shortcut.getUrl());
            }
        }
        saveSettings();
    }

    private boolean isValidImageUrl(Shortcut shortcut) {
        try (InputStream in = new URL(shortcut.getUrl()).openStream()) {
            BufferedImage image = ImageIO.read(in);
            return image != null;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isUndoEnabled() {
        return playerSettings.isUndoEnabled();
    }

    public void setUndoEnabled(boolean value) {
        playerSettings.setUndoEnabled(value);
        saveSettings();
    }

    public void setTrigger(String heldItemName) {
        playerSettings.setStampItem(heldItemName);
        saveSettings();
    }

    public void setStampActivated(boolean isStampActivated) {
        playerSettings.setStampActivated(isStampActivated);
        saveSettings();
    }

    public void setStampShortcut(String shortcutName) {
        playerSettings.setActiveShortcut(shortcutName);
        saveSettings();
    }

    public String getStampItem() {
        return playerSettings.getStampItem();
    }

    public boolean isDithering() {
        return playerSettings.isDithering();
    }

    public void setDithering(boolean value) {
        playerSettings.setDithering(value);
        saveSettings();
    }

    private void saveSettings() {
        cm.savePlayerSettings(playerSettings);
    }

    private static class UndoBufferManager {
        private static final UndoBufferManager INSTANCE = new UndoBufferManager();
        private final HashMap<String, LinkedBlockingDeque<Stack<RevertibleBlock>>> undoBuffers = new HashMap<>();

        private UndoBufferManager() {
        }

        public static UndoBufferManager getInstance() {
            return INSTANCE;
        }

        public LinkedBlockingDeque<Stack<RevertibleBlock>> getUndoBuffer(String playerName) {
            return undoBuffers.computeIfAbsent(playerName, k -> new LinkedBlockingDeque<>());
        }

        public void purgeAll() {
            undoBuffers.clear();
        }
    }
}
