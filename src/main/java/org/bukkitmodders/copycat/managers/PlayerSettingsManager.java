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
import org.bukkitmodders.copycat.plugin.RevertableBlock;
import org.bukkitmodders.copycat.schema.PlayerSettingsType;
import org.bukkitmodders.copycat.schema.PlayerSettingsType.Shortcuts;
import org.bukkitmodders.copycat.schema.PlayerSettingsType.Shortcuts.Shortcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerSettingsManager {

	private static final HashMap<String, LinkedBlockingDeque<Stack<RevertableBlock>>> undoBuffers = new HashMap<String, LinkedBlockingDeque<Stack<RevertableBlock>>>();
	private final PlayerSettingsType playerSettings;
	private final ConfigurationManager cm;
	private final Logger log = LoggerFactory.getLogger(PlayerSettingsManager.class);

	PlayerSettingsManager(PlayerSettingsType playerSettings, ConfigurationManager configurationManager) {

		this.playerSettings = playerSettings;
		this.cm = configurationManager;
	}

	public static void purgeAllUndoBuffers() {
		undoBuffers.clear();
	}

	public LinkedBlockingDeque<Stack<RevertableBlock>> getUndoBuffer() {
		String playerName = playerSettings.getPlayerName();
		if (!undoBuffers.containsKey(playerName)) {
			undoBuffers.put(playerName, new LinkedBlockingDeque<Stack<RevertableBlock>>());
		}

		return undoBuffers.get(playerName);
	}

	public Shortcut getShortcut(String name) {

		for (Shortcut shortcut : playerSettings.getShortcuts().getShortcut()) {
			if (shortcut.getName().equalsIgnoreCase(name)) {
				return shortcut;
			}
		}

		return null;
	}

	public void addShortcut(String name, String url) {

		Shortcut shortcut = getShortcut(name);

		if (shortcut == null) {
			shortcut = new Shortcut();
			playerSettings.getShortcuts().getShortcut().add(shortcut);
		}

		shortcut.setName(name);
		shortcut.setUrl(url);

		cm.savePlayerSettings(playerSettings);
	}

	public void deleteShortcut(String name) {

		Shortcut shortcut = getShortcut(name);

		if (shortcut != null) {
			playerSettings.getShortcuts().getShortcut().remove(shortcut);
		}

		cm.savePlayerSettings(playerSettings);
	}

	public void tellShortcuts(CommandSender player) {

		Shortcuts shortcuts = playerSettings.getShortcuts();

		for (Shortcut shortcut : shortcuts.getShortcut()) {
			player.sendMessage(shortcut.getName() + "=" + shortcut.getUrl());
		}
	}

	public boolean isStampModeActivated() {
		return playerSettings.isStampActivated();
	}

	public String getBlockProfile() {
		return playerSettings.getBlockProfile();
	}

	public void setBlockProfile(String blockProfileName) {
		playerSettings.setBlockProfile(blockProfileName);
		cm.savePlayerSettings(playerSettings);
	}

	public void setBuildDimensions(int width, int height) {
		playerSettings.setBuildWidth(width);
		playerSettings.setBuildHeight(height);
		cm.savePlayerSettings(playerSettings);
	}

	public int getBuildHeight() {

		return Math.min(cm.getMaxImageHeight(), playerSettings.getBuildHeight());
	}

	public int getBuildWidth() {

		return Math.min(cm.getMaxImageWidth(), playerSettings.getBuildWidth());
	}

	public Shortcut getStampShortcut() {

		Shortcut shortcut = getShortcut(playerSettings.getActiveShortcut());

		return shortcut;
	}

	public void cleanShortcuts() {
		List<Shortcut> shortcuts = playerSettings.getShortcuts().getShortcut();
		final Iterator<Shortcut> shortcutsIterator = shortcuts.iterator();

		while (shortcutsIterator.hasNext()) {
			Shortcut shortcut = shortcutsIterator.next();
			try {
				InputStream in = new URL(shortcut.getUrl()).openStream();
				BufferedImage image = ImageIO.read(in);
				image.getType();
			} catch (Exception e) {
				shortcutsIterator.remove();
				log.info("URL is not an image or is invalid. Removed: " + shortcut.getName() + " " + shortcut.getUrl());
			}
		}

		cm.savePlayerSettings(playerSettings);
	}

	public boolean isUndoEnabled() {
		return playerSettings.isUndoEnabled();
	}

	public void setUndoEnabled(boolean value) {
		playerSettings.setUndoEnabled(value);
		cm.savePlayerSettings(playerSettings);
	}

	public void setTrigger(String heldItemName) {
		playerSettings.setStampItem(heldItemName);
		cm.savePlayerSettings(playerSettings);
	}

	public void setCopyEnabled(boolean isWandActivated) {
		playerSettings.setStampActivated(isWandActivated);
		cm.savePlayerSettings(playerSettings);
	}

	public void setStampShortcut(String poll) {
		playerSettings.setActiveShortcut(poll);
		cm.savePlayerSettings(playerSettings);
	}

	public String getStampItem() {
		return playerSettings.getStampItem();
	}

	public boolean isDithering() {

		return playerSettings.isDithering();
	}

	public void setDithering(boolean value) {
		playerSettings.setDithering(value);
		cm.savePlayerSettings(playerSettings);
	}
}
