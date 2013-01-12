package org.bukkitmodders.copycat;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkitmodders.copycat.commands.ImgCommand;
import org.bukkitmodders.copycat.managers.PlayerSettingsManager;
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

		PlayerSettingsManager playerSettings = plugin.getConfigurationManager().getPlayerSettings(requestor.getName());

		boolean isBuildSet = requestor.isPermissionSet("permissions.build");
		boolean isBuilder = requestor.hasPermission("permissions.build");

		log.debug("isBuildSet: " + isBuildSet + " isBuilder: " + isBuilder);
		if (isBuildSet && !isBuilder) {
			requestor.sendMessage("You do not have permissions.build");
			return;
		}

		if (!playerSettings.isCopyEnabled()) {
			log.debug("Copying is disabled. Not doing anything.");
			return;
		}

		if (playerSettings.getActiveShortcut() == null) {
			requestor.sendMessage("Copycat is on, but you have no active image set");
			return;
		}

		log.debug(requestor + " performing copy");

		Location location = null;

		new ImgCommand(plugin).performDraw(requestor, location);
	}

	@EventHandler
	public void onEvent(PlayerInteractEntityEvent e) {
		log.debug("Player RIGHT click activation: " + e.getPlayer().getName());
	}
}
