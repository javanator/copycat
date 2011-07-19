package org.bukkitmodders.helloworld;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyPlugin extends JavaPlugin {

	private final MyPlayerListener playerListener = new MyPlayerListener(this);
	private final MyBlockListener blockListener = new MyBlockListener(this);
	private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
	private final Logger log = LoggerFactory.getLogger(MyPlugin.class);

	public void onDisable() {

		log.info("Plugin Disabled");
	}

	public void onEnable() {
		log.info("Plugin Enabled");

		// Register events we are interested in listenting to
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);

		PluginDescriptionFile pdfFile = this.getDescription();

		log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
	}

	public boolean isDebugging(final Player player) {
		if (debugees.containsKey(player)) {
			return debugees.get(player);
		} else {
			return false;
		}
	}

	public void setDebugging(final Player player, final boolean value) {
		debugees.put(player, value);
	}

}