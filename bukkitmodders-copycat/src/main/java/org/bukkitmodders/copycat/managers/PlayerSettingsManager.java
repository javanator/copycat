package org.bukkitmodders.copycat.managers;

import org.bukkit.entity.Player;
import org.bukkitmodders.copycat.schema.PlayerSettingsType;
import org.bukkitmodders.copycat.schema.PlayerSettingsType.Shortcuts;
import org.bukkitmodders.copycat.schema.PlayerSettingsType.Shortcuts.Shortcut;

public class PlayerSettingsManager {

	private final PlayerSettingsType playerSettings;
	private final ConfigurationManager cm;

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

		cm.persist();
	}

	public void deleteShortcut(String name) {

		Shortcut shortcut = getShortcut(name);

		if (shortcut != null) {
			playerSettings.getShortcuts().getShortcut().remove(shortcut);
		}

		cm.persist();
	}

	public void tellShortcuts(Player player) {

		Shortcuts shortcuts = playerSettings.getShortcuts();
		player.sendMessage("Your Shortcuts: ");

		for (Shortcut shortcut : shortcuts.getShortcut()) {
			player.sendMessage(shortcut.getName() + "=" + shortcut.getUrl());
		}
	}

	public void disable() {
		playerSettings.setPlayerEnabled(false);
		cm.persist();
	}

	public void enable() {
		playerSettings.setPlayerEnabled(true);
		cm.persist();
	}

	public boolean isEnabled() {
		return playerSettings.isPlayerEnabled();
	}

	public String getBlockProfile() {
		return playerSettings.getBlockProfile();
	}

	public void setBlockProfile(String blockProfileName) {
		playerSettings.setBlockProfile(blockProfileName);
		cm.persist();
	}

	public void setBuildWidth(int width) {
		playerSettings.setBuildWidth(width);
		cm.persist();
	}

	public void setBuildHeight(int height) {
		playerSettings.setBuildHeight(height);
		cm.persist();
	}

	public int getBuildHeight() {

		return Math.min(cm.getMaxImageHeight(), playerSettings.getBuildHeight());
	}

	public int getBuildWidth() {

		return Math.min(cm.getMaxImageWidth(), playerSettings.getBuildWidth());
	}
}
