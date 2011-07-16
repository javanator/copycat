package org.bukkitmodders.copycat;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkitmodders.copycat.functions.AbstractCopycatFunction;
import org.bukkitmodders.copycat.functions.HelpFunction;
import org.bukkitmodders.copycat.functions.ShortcutFunctions;
import org.bukkitmodders.copycat.managers.PlayerSettingsManager;
import org.bukkitmodders.copycat.schema.PlayerSettingsType.Shortcuts.Shortcut;
import org.bukkitmodders.copycat.util.NeedMoreArgumentsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CopycatCommand extends AbstractCopycatCommand {

	private final Logger log = LoggerFactory.getLogger(CopycatCommand.class);

	public CopycatCommand(Plugin instance) {

		super(instance);
	}

	@Override
	protected void performCommand(Player sender, Command command, String label, Queue<String> args)
			throws NeedMoreArgumentsException {

		PlayerSettingsManager playerSettings = getPlugin().getConfigurationManager()
				.getPlayerSettings(sender.getName());

		Map<String, AbstractCopycatFunction> functions = getPlugin().getFunctions();

		try {
			AbstractCopycatFunction.validateSufficientArgs(1, args);
		} catch (NeedMoreArgumentsException e) {
			// Help a player out
			args.add(HelpFunction.FUNCTION_STRING);
		}

		String desiredFunction = args.remove();

		if (functions.containsKey(desiredFunction)) {

			AbstractCopycatFunction function = functions.get(desiredFunction);

			try {
				function.performFunction(sender, args);
			} catch (NeedMoreArgumentsException e) {
				sender.sendMessage(e.getMessage());
				function.giveFunctionHelp(sender);
			}

		} else if (playerSettings.getShortcut(desiredFunction) != null) {

			// For convenience...
			// If the desired function matches a shortcut, copy it

			Shortcut shortcut = playerSettings.getShortcut(desiredFunction);

			if (shortcut != null) {
				Queue<String> argsCopy = new LinkedList<String>();
				argsCopy.add(shortcut.getName());
				argsCopy.addAll(args);

				((ShortcutFunctions) functions.get(ShortcutFunctions.FUNCTION_NAME)).doCopy(sender, argsCopy);
			}
		} else {
			functions.get(HelpFunction.FUNCTION_STRING).performFunction(sender, args);
		}
	}

	@Override
	public String getCommandString() {

		return getPlugin().getTriggerString();
	}

	public static Map<String, Object> getUsageDescMap() {

		HashMap<String, Object> ccCommand = new HashMap<String, Object>();

		ccCommand = new HashMap<String, Object>();
		ccCommand.put("description", "Copycat renders images into the game world");
		ccCommand.put("usage", "Type /cc help for help");

		return ccCommand;
	}
}
