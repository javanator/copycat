package org.bukkitmodders.copycat.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkitmodders.copycat.Application;

public class PlayerListener implements Listener {
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

    }

    @EventHandler
    public void onPlayerExit(PlayerQuitEvent event) {
        //Stop their mediaPlayer streams
        Application.getInstance().getMediaService().stopVideoStreamsForPlayer(event.getPlayer());
    }
}
