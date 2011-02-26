package org.bukkitmodders.helloworld;

import org.bukkit.event.player.PlayerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyPlayerListener extends PlayerListener {

	private final Logger log = LoggerFactory.getLogger(PlayerListener.class);
	private final MyPlugin plugin;

	public MyPlayerListener(MyPlugin instance) {
		plugin = instance;
	}

	// Override player events
}
