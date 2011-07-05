package org.bukkitmodders.copycat;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCopycatCommand implements CommandExecutor {

	private final Logger log = LoggerFactory.getLogger(AbstractCopycatCommand.class);
	private final Plugin plugin;

	public AbstractCopycatCommand(Plugin plugin) {

		this.plugin = plugin;
	}

	protected Plugin getPlugin() {
		return this.plugin;
	}

	@Override
	final public boolean onCommand(final CommandSender sender, final Command command, final String label,
			final String[] args) {

		if (sender instanceof Player) {

			Queue<String> argsQueue = new LinkedList<String>();
			argsQueue.addAll(Arrays.asList(args));

			performCommand((Player) sender, command, label, argsQueue);
		}

		return true;
	}

	protected abstract void performCommand(Player sender, Command command, String label, Queue<String> argsQueue);

	public abstract String getCommandString();

	public abstract void appendYml(String indent, StringBuffer yml);
}
