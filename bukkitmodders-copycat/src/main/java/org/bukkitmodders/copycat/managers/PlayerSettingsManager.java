package org.bukkitmodders.copycat.managers;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import org.bukkit.entity.Player;
import org.bukkitmodders.copycat.schema.PlayerSettingsType;
import org.bukkitmodders.copycat.schema.PlayerSettingsType.Shortcuts;
import org.bukkitmodders.copycat.schema.PlayerSettingsType.Shortcuts.Shortcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerSettingsManager {

	private final PlayerSettingsType playerSettings;
	private final ConfigurationManager cm;
	private final Logger log = LoggerFactory.getLogger(PlayerSettingsManager.class);

	PlayerSettingsManager(PlayerSettingsType playerSettings, ConfigurationManager configurationManager) {

		this.playerSettings = playerSettings;
		this.cm = configurationManager;
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

	public void tellShortcuts(Player player) {

		Shortcuts shortcuts = playerSettings.getShortcuts();
		player.sendMessage("Your Shortcuts: ");

		for (Shortcut shortcut : shortcuts.getShortcut()) {
			player.sendMessage(shortcut.getName() + "=" + shortcut.getUrl());
		}
	}

	public boolean isCopyEnabled() {
		return playerSettings.isCopyEnabled();
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

	public Shortcut getActiveShortcut() {

		Shortcut shortcut = getShortcut(playerSettings.getActiveShortcut());

		return shortcut;
	}

	public void cleanShortcuts(Player player) {
		List<Shortcut> shortcuts = playerSettings.getShortcuts().getShortcut();
		Iterator<Shortcut> shortcutsIterator = shortcuts.iterator();

		while (shortcutsIterator.hasNext()) {
			Shortcut shortcut = shortcutsIterator.next();
			try {
				InputStream in = new URL(shortcut.getUrl()).openStream();
				BufferedImage image = ImageIO.read(in);
				image.getType();
			} catch (Exception e) {
				shortcutsIterator.remove();
				player.sendMessage("URL is not an image or is invalid. Removed: " + shortcut.getName() + " " + shortcut.getUrl());
			}
		}

		cm.savePlayerSettings(playerSettings);
	}

	public void setTrigger(String heldItemName) {
		playerSettings.setTrigger(heldItemName);
		cm.savePlayerSettings(playerSettings);
	}

	public void setCopyEnabled(boolean isCopyEnabled) {
		playerSettings.setCopyEnabled(isCopyEnabled);
		cm.savePlayerSettings(playerSettings);
	}

	public void setActiveShortcut(String poll) {
		playerSettings.setActiveShortcut(poll);
		cm.savePlayerSettings(playerSettings);
	}
}
