package org.bukkitmodders.copycat.plugin;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkitmodders.copycat.Plugin;
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
	final public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {

		if (sender instanceof Player) {

			try {
				Queue<String> argsQueue = new LinkedList<String>();
				argsQueue.addAll(Arrays.asList(args));

				performCommand((Player) sender, command, label, argsQueue);
			} catch (Exception e) {

				log.error("Caught exception performing command with args: " + Arrays.deepToString(args), e);

				if (e.getMessage() != null) {
					sender.sendMessage(e.getMessage());
				} else {
					sender.sendMessage("Something very unexpected happened. See server log.");
				}
			}
		}

		return true;
	}

	protected abstract void performCommand(Player sender, Command command, String label, Queue<String> argsQueue) throws Exception;

	public abstract String getCommandString();
}
