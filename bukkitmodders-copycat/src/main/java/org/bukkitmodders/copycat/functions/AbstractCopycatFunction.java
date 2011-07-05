package org.bukkitmodders.copycat.functions;

import java.util.Queue;
import java.util.StringTokenizer;

import org.bukkit.entity.Player;
import org.bukkitmodders.copycat.CopycatCommand;
import org.bukkitmodders.copycat.Plugin;
import org.bukkitmodders.copycat.util.NeedMoreArgumentsException;
import org.bukkitmodders.copycat.util.NotAnOpException;

public abstract class AbstractCopycatFunction {

	private final Plugin plugin;

	AbstractCopycatFunction(Plugin plugin) {
		this.plugin = plugin;
	}

	public abstract void performFunction(Player sender, Queue<String> args) throws NeedMoreArgumentsException;

	public abstract String getFunction();

	abstract boolean isNeedsOpPermissions();

	protected String getArg(int i, String[] split) {

		if (i < split.length) {
			return split[i];
		}

		return null;
	}

	protected void tellSplitNewline(Player player, StringBuffer sb) {
		StringTokenizer st = new StringTokenizer(sb.toString(), "\n");

		while (st.hasMoreTokens()) {
			player.sendMessage(st.nextToken());
		}
	}

	protected String getOperationPrefix() {
		return "/" + CopycatCommand.CC + " " + getFunction() + " ";
	}

	public void giveFunctionHelp(Player player) {

		StringBuffer sb = new StringBuffer();
		buildFunctionHelp(sb);

		tellSplitNewline(player, sb);
	}

	public static void validateSufficientArgs(int expectedNumArgs, Queue<String> args)
			throws NeedMoreArgumentsException {

		if (args.size() < expectedNumArgs) {

			throw new NeedMoreArgumentsException("Wrong Number of Arguments");
		}
	}

	public abstract void buildFunctionHelp(StringBuffer sb);

	protected void performOpCheck(Player player) throws NotAnOpException {

		if (!player.isOp()) {
			throw new NotAnOpException("You are not an OP");
		}
	}

	public Plugin getPlugin() {
		return plugin;
	}
}
