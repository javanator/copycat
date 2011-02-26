package org.bukkitmodders.helloworld;

import org.bukkit.event.block.BlockListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyBlockListener extends BlockListener {

	private final MyPlugin plugin;

	private Logger log = LoggerFactory.getLogger(MyBlockListener.class);

	public MyBlockListener(final MyPlugin plugin) {
		this.plugin = plugin;
	}

	// Override block events in super.
}
