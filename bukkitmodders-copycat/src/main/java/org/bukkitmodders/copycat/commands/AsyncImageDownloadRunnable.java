package org.bukkitmodders.copycat.commands;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkitmodders.copycat.Nouveau;
import org.bukkitmodders.copycat.managers.PlayerSettingsManager;
import org.bukkitmodders.copycat.schema.PlayerSettingsType.Shortcuts.Shortcut;

final class AsyncImageDownloadRunnable implements Runnable {
	/**
	 * 
	 */
	private final PlayerSettingsManager playerSettings;
	private final CommandSender sender;
	private final Location location;
	private final Nouveau plugin;

	AsyncImageDownloadRunnable(PlayerSettingsManager playerSettings, CommandSender sender, Location location, Nouveau plugin) {
		this.playerSettings = playerSettings;
		this.sender = sender;
		this.location = location;
		this.plugin = plugin;
	}

	@Override
	public void run() {
		InputStream in = null;

		try {
			Shortcut shortcut = playerSettings.getActiveShortcut();
			in = new URL(shortcut.getUrl()).openStream();
			final BufferedImage image = ImageIO.read(in);

			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

				@Override
				public void run() {
					new CCCommand(plugin).performDraw(sender, location, image);
				}
			});

		} catch (MalformedURLException e) {
			sender.sendMessage("Bad URL. Please check");
			ImgCommand.log.error("Bad URL", e);
		} catch (IOException e) {
			sender.sendMessage("Error reading image");
			ImgCommand.log.error("Error reading image", e);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}
}