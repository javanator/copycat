package org.bukkitmodders.copycat.functions;

import java.util.Queue;

import org.bukkit.entity.Player;
import org.bukkitmodders.copycat.Plugin;
import org.bukkitmodders.copycat.managers.ConfigurationManager;
import org.bukkitmodders.copycat.managers.PlayerSettingsManager;
import org.bukkitmodders.copycat.plugin.NeedMoreArgumentsException;
import org.bukkitmodders.copycat.plugin.NotAnOpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpFunctions extends AbstractCopycatFunction {

	static final String FUNCTION_NAME = "op";
	private Logger log = LoggerFactory.getLogger(OpFunctions.class);

	public OpFunctions(Plugin plugin) {
		super(plugin);
	}

	@Override
	public void buildFunctionHelp(StringBuffer sb) {
		sb.append("==== OP Function Help =====\n");
		sb.append("/" + getOperationPrefix() + " undo <player name> - Undoes player's last copy\n");
		sb.append("/" + getOperationPrefix() + " enable <player name> - Enables plugin for a player\n");
		sb.append("/" + getOperationPrefix() + " disable <player name> - Disables plugin for a player\n");
		sb.append("/" + getOperationPrefix() + " enableWorld <world name> - Enables plugin in a world\n");
		sb.append("/" + getOperationPrefix() + " disableWorld <world name> - Disables plugin in a world\n");
	}

	@Override
	public void performFunction(Player sender, Queue<String> args) throws NeedMoreArgumentsException {

		try {

			performOpCheck(sender);

			validateSufficientArgs(1, args);

			String operation = args.remove();

			if ("enable".equalsIgnoreCase(operation)) {
				doEnable(sender, args);
			} else if ("disable".equalsIgnoreCase(operation)) {
				doDisable(sender, args);
			} else if ("enableWorld".equalsIgnoreCase(operation)) {
				doEnableWorld(sender, args);
			} else if ("disableWorld".equalsIgnoreCase(operation)) {
				doDisableWorld(sender, args);
			} else if ("undo".equalsIgnoreCase(operation)) {
				doUndoPlayer(sender, args);
			}

		} catch (NotAnOpException e) {
			sender.sendMessage(e.getMessage());
			log.info("Player tried to perform operator function: " + sender.getName());
		}
	}

	private void doUndoPlayer(Player sender, Queue<String> args) throws NeedMoreArgumentsException {

		validateSufficientArgs(1, args);
		String playerName = args.remove();

		UndoFunction undoFunction = (UndoFunction) getPlugin().getFunctions().get(UndoFunction.FUNCTION_STRING);
		undoFunction.doUndo(sender, playerName);
	}

	@Override
	public String getFunction() {
		return FUNCTION_NAME;
	}

	private void doDisable(Player requestor, Queue<String> args) throws NeedMoreArgumentsException {
		validateSufficientArgs(1, args);

		String playerName = args.remove();

		PlayerSettingsManager playerSettings = getPlugin().getConfigurationManager().getPlayerSettings(playerName);
		playerSettings.disable();

		requestor.sendMessage(playerName + " disabled");
	}

	private void doEnable(Player requestor, Queue<String> args) throws NeedMoreArgumentsException {

		validateSufficientArgs(1, args);

		String playerName = args.remove();

		PlayerSettingsManager playerSettings = getPlugin().getConfigurationManager().getPlayerSettings(playerName);
		playerSettings.enable();

		requestor.sendMessage(playerName + " enabled");

	}

	private void doDisableWorld(Player requestor, Queue<String> args) throws NeedMoreArgumentsException {

		validateSufficientArgs(1, args);

		String worldName = args.remove();

		ConfigurationManager configurationManager = getPlugin().getConfigurationManager();
		configurationManager.disableWorld(worldName);

		requestor.sendMessage("World Disabled");
	}

	private void doEnableWorld(Player requestor, Queue<String> args) throws NeedMoreArgumentsException {

		validateSufficientArgs(1, args);
		String worldName = args.remove();

		ConfigurationManager configurationManager = getPlugin().getConfigurationManager();
		configurationManager.enableWorld(worldName);

		requestor.sendMessage("World Enabled");
	}

	@Override
	protected boolean isNeedsOpPermissions() {
		return true;
	}
}
