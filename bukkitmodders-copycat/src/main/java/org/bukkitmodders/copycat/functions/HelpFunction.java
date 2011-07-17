package org.bukkitmodders.copycat.functions;

import java.util.Map;
import java.util.Queue;

import org.bukkit.entity.Player;
import org.bukkitmodders.copycat.Plugin;

public class HelpFunction extends AbstractCopycatFunction {

	public static final String FUNCTION_STRING = "help";

	public HelpFunction(Plugin plugin) {
		super(plugin);
	}

	@Override
	public void performFunction(Player requestor, Queue<String> args) {
		doHelp(requestor);
	}

	@Override
	public void buildFunctionHelp(StringBuffer sb) {
		sb.append("/" + getOperationPrefix() + " - Shows help\n");
	}

	public void doHelp(Player player) {

		StringBuffer sb = new StringBuffer();
		sb.append("Copycat Help:\n");

		Map<String, AbstractCopycatFunction> functions = getPlugin().getFunctions();
		AbstractCopycatFunction function = null;

		if (player.isOp()) {
			function = functions.get(OpFunctions.FUNCTION_NAME);
			function.buildFunctionHelp(sb);
		}

		function = functions.get(ShortcutFunctions.FUNCTION_NAME);
		function.buildFunctionHelp(sb);

		function = functions.get(SetFunction.FUNCTION_STRING);
		function.buildFunctionHelp(sb);

		function = functions.get(UndoFunction.FUNCTION_STRING);
		function.buildFunctionHelp(sb);

		function = functions.get(HelpFunction.FUNCTION_STRING);
		function.buildFunctionHelp(sb);

		sb.append("\n");
		sb.append("/" + getOperationPrefix() + "<shortcut name> - Same as /cc shortuct copy <shortcut name>");

		tellSplitNewline(player, sb);
	}

	@Override
	public String getFunction() {
		return FUNCTION_STRING;
	}

	@Override
	protected boolean isNeedsOpPermissions() {
		return false;
	}
}
