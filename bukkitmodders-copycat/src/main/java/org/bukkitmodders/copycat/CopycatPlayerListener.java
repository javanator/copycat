package org.bukkitmodders.copycat;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingDeque;

import javax.imageio.ImageIO;
import javax.vecmath.Matrix4d;

import org.apache.commons.io.IOUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkitmodders.copycat.managers.ConfigurationManager;
import org.bukkitmodders.copycat.managers.PlayerSettingsManager;
import org.bukkitmodders.copycat.plugin.RevertableBlock;
import org.bukkitmodders.copycat.schema.BlockProfileType;
import org.bukkitmodders.copycat.schema.PlayerSettingsType.Shortcuts.Shortcut;
import org.bukkitmodders.copycat.services.ImageCopier;
import org.bukkitmodders.copycat.util.ImageUtil;
import org.bukkitmodders.copycat.util.MatrixUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CopycatPlayerListener implements Listener {

	private final Nouveau plugin;
	private static final Logger log = LoggerFactory.getLogger(CopycatPlayerListener.class);

	public CopycatPlayerListener(Nouveau plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onEvent(PlayerInteractEvent e) {
		log.debug("Player LEFT click activation: " + e.getPlayer().getName());

		Player requestor = e.getPlayer();

		ConfigurationManager configurationManager = plugin.getConfigurationManager();
		PlayerSettingsManager playerSettings = plugin.getConfigurationManager().getPlayerSettings(requestor.getName());

		if (playerSettings.getActiveShortcut() == null) {
			requestor.sendMessage("Copycat is on, but you have no active image set");
			return;
		}
		
		if (!configurationManager.isWorldEnabled(requestor.getWorld().getName())) {
			requestor.sendMessage("World is disabled");
			return;
		}

		log.debug(requestor + " performing copy");

		Block targetBlock = requestor.getTargetBlock(null, 100);

		Shortcut shortcut = playerSettings.getActiveShortcut();
		InputStream in = null;

		try {
			in = new URL(shortcut.getUrl()).openStream();

			BufferedImage image = ImageIO.read(in);

			image = ImageUtil.scaleImage(image, playerSettings.getBuildWidth(), playerSettings.getBuildHeight());

			requestor.sendMessage("Copying your image: " + shortcut.getUrl());
			requestor.sendMessage("Width: " + image.getWidth() + " Height: " + image.getHeight());

			Matrix4d rotationMatrix = null;
			Location location = new Location(requestor.getWorld(), targetBlock.getX(), targetBlock.getY(), targetBlock.getZ());
			rotationMatrix = MatrixUtil.calculateRotation(requestor.getLocation());

			BlockProfileType blockProfile = configurationManager.getBlockProfile(playerSettings.getBlockProfile());
			Stack<RevertableBlock> undoBuffer = createUndoBuffer(requestor);
			ImageCopier mcGraphics2d = new ImageCopier(blockProfile, location, requestor.getWorld(), rotationMatrix);

			mcGraphics2d.draw(image, undoBuffer);

		} catch (MalformedURLException ioe) {
			requestor.sendMessage("Bad URL");
			log.error("Bad URL during copycat copy", ioe);
		} catch (IOException ioe) {
			requestor.sendMessage("Error reading shortcut");
			log.error("Error reading shortcut", ioe);
		} finally {
			IOUtils.closeQuietly(in);
		}

	}

	@EventHandler
	public void onEvent(PlayerInteractEntityEvent e) {
		log.debug("Player RIGHT click activation: " + e.getPlayer().getName());
	}



	private Stack<RevertableBlock> createUndoBuffer(Player player) {
		Stack<RevertableBlock> undoBuffer = new Stack<RevertableBlock>();
		HashMap<String, LinkedBlockingDeque<Stack<RevertableBlock>>> undoBuffers = plugin.getUndoBuffers();
		String playerName = player.getName();

		if (!undoBuffers.containsKey(playerName)) {
			undoBuffers.put(playerName, new LinkedBlockingDeque<Stack<RevertableBlock>>(Settings.MAX_PLAYER_UNDO));
		}

		undoBuffers.get(playerName).push(undoBuffer);
		return undoBuffer;
	}
}
