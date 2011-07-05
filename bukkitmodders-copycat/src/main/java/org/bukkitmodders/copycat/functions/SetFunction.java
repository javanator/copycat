package org.bukkitmodders.copycat.functions;

import java.util.Arrays;
import java.util.Queue;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkitmodders.copycat.Plugin;
import org.bukkitmodders.copycat.managers.ConfigurationManager;
import org.bukkitmodders.copycat.managers.PlayerSettingsManager;
import org.bukkitmodders.copycat.util.NeedMoreArgumentsException;

public class SetFunction extends AbstractCopycatFunction {

	static final String FUNCTION_STRING = "set";

	public SetFunction(Plugin plugin) {
		super(plugin);
	}

	@Override
	public void performFunction(Player sender, Queue<String> args) throws NeedMoreArgumentsException,
			NeedMoreArgumentsException {

		if (args.isEmpty()) {
			doShow(sender);
			return;
		}

		String operation = args.remove();

		if ("dimensions".equalsIgnoreCase(operation)) {
			doSetDimensions(sender, args);
		} else if ("blockProfile".equalsIgnoreCase(operation)) {
			doSetBlockProfile(sender, args);
		} else {
			sender.sendMessage("Invalid Operation");
		}
	}

	private void doSetBlockProfile(Player sender, Queue<String> args) throws NeedMoreArgumentsException {

		if (args.isEmpty()) {
			Set<String> blockProfileNames = getPlugin().getConfigurationManager().getBlockProfiles().keySet();
			sender.sendMessage("Available Block Profiles: " + Arrays.deepToString(blockProfileNames.toArray()));

			return;
		}

		PlayerSettingsManager playerSettings = getPlugin().getConfigurationManager()
				.getPlayerSettings(sender.getName());

		String profile = args.remove();
		if (!getPlugin().getConfigurationManager().getBlockProfiles().containsKey(profile)) {
			sender.sendMessage("Invalid Block Profile");
		} else {
			playerSettings.setBlockProfile(profile);
			sender.sendMessage("Block Profile Changed");
		}

	}

	private void doShow(Player sender) {

		ConfigurationManager configurationManager = getPlugin().getConfigurationManager();
		PlayerSettingsManager playerSettings = configurationManager.getPlayerSettings(sender.getName());

		sender.sendMessage("Your Current Build Options:");
		sender.sendMessage("Dimensions: " + playerSettings.getBuildWidth() + " x " + playerSettings.getBuildHeight());
		sender.sendMessage("Block Profile: " + playerSettings.getBlockProfile());
	}

	private void doSetDimensions(Player sender, Queue<String> args) throws NeedMoreArgumentsException {

		validateSufficientArgs(2, args);

		PlayerSettingsManager playerSettings = getPlugin().getConfigurationManager()
				.getPlayerSettings(sender.getName());
		playerSettings.setBuildWidth(Integer.parseInt(args.remove()));
		playerSettings.setBuildHeight(Integer.parseInt(args.remove()));

		sender.sendMessage("Build Dimensions Changed");
	}

	@Override
	public String getFunction() {

		return FUNCTION_STRING;
	}

	@Override
	boolean isNeedsOpPermissions() {

		return false;
	}

	@Override
	public void buildFunctionHelp(StringBuffer sb) {
		getPlugin().getConfigurationManager().getBlockProfiles().keySet();
		sb.append("===== Set Functions =====\n");
		sb.append(getOperationPrefix() + "(no args) - show all settings\n");
		sb.append(getOperationPrefix() + " dimensions <width> <height> - Set render dimensions\n");
		sb.append(getOperationPrefix() + "blockProfile <name> - Block profile to use\n");
		sb.append(getOperationPrefix() + "blockProfile <no args> - Available Block Profiles\n");
	}
}
