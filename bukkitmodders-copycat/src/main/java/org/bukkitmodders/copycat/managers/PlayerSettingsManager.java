package org.bukkitmodders.copycat.managers;

import org.bukkit.entity.Player;
import org.bukkitmodders.copycat.schema.PlayerSettingsType;
import org.bukkitmodders.copycat.schema.PlayerSettingsType.Shortcuts;
import org.bukkitmodders.copycat.schema.PlayerSettingsType.Shortcuts.Shortcut;
import org.bukkitmodders.copycat.schema.PluginConfig;

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

	public void disable() {
		playerSettings.setPlayerEnabled(false);
		cm.savePlayerSettings(playerSettings);
	}

	public void enable() {
		playerSettings.setPlayerEnabled(true);
		cm.savePlayerSettings(playerSettings);
	}

	public boolean isEnabled() {
		return playerSettings.isPlayerEnabled();
	}

	public String getBlockProfile() {
		return playerSettings.getBlockProfile();
	}

	public void setBlockProfile(String blockProfileName) {
		playerSettings.setBlockProfile(blockProfileName);
		cm.savePlayerSettings(playerSettings);
	}

	public void setBuildWidth(int width) {
		playerSettings.setBuildWidth(width);
		cm.savePlayerSettings(playerSettings);
	}

	public void setBuildHeight(int height) {
		playerSettings.setBuildHeight(height);
		cm.savePlayerSettings(playerSettings);
	}

	public int getBuildHeight() {

		return Math.min(cm.getMaxImageHeight(), playerSettings.getBuildHeight());
	}

	public int getBuildWidth() {

		return Math.min(cm.getMaxImageWidth(), playerSettings.getBuildWidth());
	}
}
