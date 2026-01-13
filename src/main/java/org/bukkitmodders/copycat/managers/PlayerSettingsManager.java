package org.bukkitmodders.copycat.managers;

import io.papermc.paper.util.Tick;
import org.bukkitmodders.copycat.Application;
import org.bukkitmodders.copycat.model.PlayerSettingsType;
import org.bukkitmodders.copycat.model.PlayerSettingsType.Shortcut;
import org.bukkitmodders.copycat.model.PolledSourceType;
import org.bukkitmodders.copycat.model.RevertibleBlock;
import org.bukkitmodders.copycat.model.UndoHistoryComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

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

    public PlayerSettingsManager(final PlayerSettingsType playerSettings, final ConfigurationManager configurationManager) {
        this.playerSettings = playerSettings;
        this.cm = configurationManager;
        this.undoBufferManager = UndoBufferManager.getInstance();
    }

    public static void purgeAllUndoBuffers() {
        UndoBufferManager.getInstance().purgeAll();
    }

    public LinkedBlockingDeque<UndoHistoryComponent> getUndoBuffer() {
        return undoBufferManager.getUndoBuffer(playerSettings.getPlayerName());
    }

    public Shortcut getShortcut(final String name) {
        return findShortcutByName(name);
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

    public boolean isStampModeActivated() {


        return playerSettings.isStampActivated();
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
        return Math.min(cm.getMaxImageHeight(), playerSettings.getBuildHeight());
    }

    public int getMaxBuildWidth() {
        return Math.min(cm.getMaxImageWidth(), playerSettings.getBuildWidth());
    }

    public Shortcut getStampShortcut() {
        return findShortcutByName(playerSettings.getActiveShortcut());
    }

    public void cleanShortcuts() {
        final List<Shortcut> shortcuts = playerSettings.getShortcuts();
        if (shortcuts == null) return;

        final Iterator<Shortcut> it = shortcuts.iterator();
        while (it.hasNext()) {
            final Shortcut shortcut = it.next();
            if (!isValidImageUrl(shortcut)) {
                it.remove();
                log.info("Removed invalid image URL shortcut: name='{}' url='{}'", shortcut.getName(), shortcut.getUrl());
            }
        }
        saveSettings();
    }

    private boolean isValidImageUrl(final Shortcut shortcut) {
        if (shortcut == null || shortcut.getUrl() == null || shortcut.getUrl().isBlank()) return false;
        return withImageFromUrl(shortcut.getUrl()) != null;
    }

    private BufferedImage withImageFromUrl(final String url) {
        try (InputStream in = new URL(url).openStream()) {
            return ImageIO.read(in);
        } catch (Exception e) {
            return null;
        }
    }

    public void setStampActivated(final boolean isStampActivated) {
        playerSettings.setStampActivated(isStampActivated);
        saveSettings();
    }

    public void setStampShortcut(final String shortcutName) {
        playerSettings.setActiveShortcut(shortcutName);
        saveSettings();
    }

    public String getStampItem() {
        return playerSettings.getStampItem();
    }

    public boolean isDithering() {
        return playerSettings.isDithering();
    }

    public void setDithering(final boolean value) {
        playerSettings.setDithering(value);
        saveSettings();
    }

    public void undo(org.bukkit.entity.Player player) {
        player.sendMessage("Undo for " + player.getName());
        LinkedBlockingDeque<UndoHistoryComponent> buffer = getUndoBuffer();

        if (!buffer.isEmpty()) {
            UndoHistoryComponent lastUndo = buffer.pop();

            if (lastUndo.getMediaPlayer() != null) {
                lastUndo.getMediaPlayer().stop();
                lastUndo.getMediaPlayer().release();
                //Let the media player handle the undo with its own lifecycle methods
            } else {
                Stack<RevertibleBlock> lastImageBlocks = lastUndo.getBlocks();

                while (!lastImageBlocks.isEmpty()) {
                    lastImageBlocks.pop().revert();
                }
            }
        }
    }

    private void saveSettings() {
        cm.savePlayerSettings(playerSettings);
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

    private static class UndoBufferManager {
        private static final UndoBufferManager INSTANCE = new UndoBufferManager();
        private final Map<String, LinkedBlockingDeque<UndoHistoryComponent>> undoBuffers = new java.util.HashMap<>();

        private UndoBufferManager() {
        }

        public static UndoBufferManager getInstance() {
            return INSTANCE;
        }

        public LinkedBlockingDeque<UndoHistoryComponent> getUndoBuffer(final String playerName) {
            return undoBuffers.computeIfAbsent(playerName, k -> new LinkedBlockingDeque<>());
        }

        public void purgeAll() {
            undoBuffers.clear();
        }
    }
}
