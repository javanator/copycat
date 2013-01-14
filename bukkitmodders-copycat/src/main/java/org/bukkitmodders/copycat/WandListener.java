package org.bukkitmodders.copycat;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkitmodders.copycat.commands.CCCommand;
import org.bukkitmodders.copycat.commands.SetCommand;
import org.bukkitmodders.copycat.commands.WandCommand;
import org.bukkitmodders.copycat.managers.PlayerSettingsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WandListener implements Listener {

	private final Nouveau plugin;
	private static final Logger log = LoggerFactory.getLogger(WandListener.class);

	public WandListener(Nouveau plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onEvent(PlayerInteractEvent e) {

		Player player = e.getPlayer();

		PlayerSettingsManager playerSettings = plugin.getConfigurationManager().getPlayerSettings(player.getName());
		boolean isBuildSet = player.isPermissionSet("permissions.build");
		boolean isBuilder = player.hasPermission("permissions.build");
		ItemStack itemInHand = player.getItemInHand();
		if (isBuildSet && !isBuilder) {
			return;
		} else if (!player.hasPermission(WandCommand.getPermissionNode())) {
			player.sendMessage("You do not have permission: " + WandCommand.getPermissionNode());
			return;
		} else if (!playerSettings.isWandActivated()) {
			// Wand mode is not activated
			return;
		} else if (!playerSettings.getTrigger().equals(itemInHand.getType().name())) {
			// Wand item not equipped.
			return;
		} else if (playerSettings.getActiveShortcut() == null) {
			player.sendMessage("Copycat is on, but you have no active image set. Use /" + SetCommand.getCommandString());
			return;
		}

		player.sendMessage("Rendering your image from: " + playerSettings.getActiveShortcut().getUrl());
		player.sendMessage("*PLEASE* be patient. DO NOT click again until render is complete.");

		Block targetBlock = player.getTargetBlock(null, 100);
		Location location = new Location(player.getWorld(), targetBlock.getX(), targetBlock.getY(), targetBlock.getZ());
		location.setPitch(player.getLocation().getPitch());
		location.setYaw(player.getLocation().getYaw());

		new CCCommand(plugin).asyncDownloadAndCopy(player, playerSettings.getActiveShortcut(), location);
	}
}
