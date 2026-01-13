package org.bukkitmodders.copycat.model;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkitmodders.copycat.Application;
import org.bukkitmodders.copycat.managers.PlayerSettingsManager;

public class BuildContextFactory {

    public static BuildContext create(Application application, Player player, PlayerSettingsType.Shortcut shortcut) {
        PlayerSettingsManager playerSettings = application.getPlayerSettings(player.getName());
        
        Block targetBlock = player.getTargetBlock(null, 100);
        Location location = targetBlock.getLocation();
        location.setYaw(player.getLocation().getYaw());
        location.setPitch(player.getLocation().getPitch());

        String blockProfileName = playerSettings.getBlockProfile();
        BlockProfileType blockProfile = application.getConfigurationManager().getBlockProfile(blockProfileName);

        return BuildContext.builder()
                .withPlayer(player)
                .withLocation(location)
                .withBlockProfile(blockProfile)
                .withShortcut(shortcut)
                .build();
    }
}
