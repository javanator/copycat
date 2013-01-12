package org.bukkitmodders.copycat;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkitmodders.copycat.commands.CCCommand;
import org.bukkitmodders.copycat.managers.PlayerSettingsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WandListener implements Listener {

	private final Nouveau plugin;
	private static final Logger log = LoggerFactory.getLogger(WandListener.class);

	public WandListener(Nouveau plugin) {
		this.plugin = plugin;
	}

	public static Map<String, Object> getPermissions() {

		Map<String, Object> permissions = new LinkedHashMap<String, Object>();
		permissions.put("description", "Renders images in-game via item. Must already have permission.build");
		permissions.put("default", "false");

		return permissions;
	}

	public static String getPermissionNode() {
		return "copycat.wandmode";
	}

	@EventHandler
	public void onEvent(PlayerInteractEvent e) {

		Player player = e.getPlayer();

		PlayerSettingsManager playerSettings = plugin.getConfigurationManager().getPlayerSettings(player.getName());
		boolean isBuildSet = player.isPermissionSet("permissions.build");
		boolean isBuilder = player.hasPermission("permissions.build");

		if (isBuildSet && !isBuilder) {
			return;
		} else if (!playerSettings.isWandActivated()) {
			// Wand mode is not activated
			return;
		} else if (!playerSettings.getTrigger().equals(player.getItemInHand().getType().name())) {
			// Wand item not equipped.
			return;
		} else if (playerSettings.getActiveShortcut() == null) {
			player.sendMessage("Copycat is on, but you have no active image set");
			return;
		}

		log.debug(player + " rendering your image");

		Block targetBlock = player.getTargetBlock(null, 100);
		Location location = new Location(player.getWorld(), targetBlock.getX(), targetBlock.getY(), targetBlock.getZ());
		location.setPitch(player.getLocation().getPitch());
		location.setYaw(player.getLocation().getYaw());
		new CCCommand(plugin).asyncDownloadAndCopy(player, location);
	}
}
