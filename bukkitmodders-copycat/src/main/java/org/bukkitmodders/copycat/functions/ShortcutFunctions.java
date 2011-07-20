package org.bukkitmodders.copycat.functions;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingDeque;

import javax.imageio.ImageIO;
import javax.vecmath.Matrix4d;

import org.apache.commons.io.IOUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkitmodders.copycat.Plugin;
import org.bukkitmodders.copycat.Settings;
import org.bukkitmodders.copycat.managers.ConfigurationManager;
import org.bukkitmodders.copycat.managers.PlayerSettingsManager;
import org.bukkitmodders.copycat.plugin.NeedMoreArgumentsException;
import org.bukkitmodders.copycat.plugin.RevertableBlock;
import org.bukkitmodders.copycat.schema.BlockProfileType;
import org.bukkitmodders.copycat.schema.PlayerSettingsType.Shortcuts.Shortcut;
import org.bukkitmodders.copycat.services.ImageCopier;
import org.bukkitmodders.copycat.util.ImageUtil;
import org.bukkitmodders.copycat.util.MatrixUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShortcutFunctions extends AbstractCopycatFunction {

	public static final String FUNCTION_NAME = "shortcut";
	private Logger log = LoggerFactory.getLogger(ShortcutFunctions.class);

	public ShortcutFunctions(Plugin plugin) {
		super(plugin);
	}

	@Override
	public void buildFunctionHelp(StringBuffer sb) {
		sb.append("==== Shortcut Function Help ====\n");
		sb.append("/" + getOperationPrefix() + " copy <shortcut name> - Copies Image\n");
		sb.append("/" + getOperationPrefix() + " add <shortcut name> <image url> - Add shortcut\n");
		sb.append("/" + getOperationPrefix() + " delete <shortcut name> - Deletes shortcut\n");
		sb.append("/" + getOperationPrefix() + " list - Lists shortcuts\n");
	}

	@Override
	public void performFunction(Player player, Queue<String> args) throws NeedMoreArgumentsException {

		validateSufficientArgs(1, args);

		String operation = args.remove();

		if ("add".equalsIgnoreCase(operation)) {
			doAdd(player, player.getName(), args);
		} else if ("delete".equalsIgnoreCase(operation)) {
			doDelete(player, player.getName(), args);
		} else if ("list".equalsIgnoreCase(operation)) {
			doList(player, player.getName());
		} else if ("copy".equalsIgnoreCase(operation)) {
			doCopy(player, args);
		} else {
			player.sendMessage("Bad Shortcut Operation");
		}
	}

	private void doDelete(Player requestor, String playerName, Queue<String> args) throws NeedMoreArgumentsException {

		validateSufficientArgs(1, args);

		String shortcutName = args.remove();

		PlayerSettingsManager playerSettings = getPlugin().getConfigurationManager().getPlayerSettings(playerName);
		playerSettings.deleteShortcut(shortcutName);

		requestor.sendMessage("Shortcut deleted");
	}

	private void doAdd(Player requestor, String playerName, Queue<String> args) throws NeedMoreArgumentsException {

		validateSufficientArgs(2, args);

		String shortcutName = args.remove();
		String shortcutValue = args.remove();

		PlayerSettingsManager playerSettings = getPlugin().getConfigurationManager().getPlayerSettings(playerName);
		playerSettings.addShortcut(shortcutName, shortcutValue);

		requestor.sendMessage("Shortcut added");
	}

	private void doList(Player requestor, String playerName) {

		ConfigurationManager configurationManager = getPlugin().getConfigurationManager();

		PlayerSettingsManager playerSettings = configurationManager.getPlayerSettings(playerName);
		playerSettings.tellShortcuts(requestor);
	}

	public void doCopy(Player requestor, Queue<String> args) throws NeedMoreArgumentsException {

		validateSufficientArgs(1, args);

		String shortcutName = args.remove();
		ConfigurationManager configurationManager = getPlugin().getConfigurationManager();

		PlayerSettingsManager playerSettings = getPlugin().getConfigurationManager().getPlayerSettings(requestor.getName());

		if (!playerSettings.isEnabled()) {
			requestor.sendMessage("You are disabled");
			return;
		}

		if (!configurationManager.isWorldEnabled(requestor.getWorld().getName())) {
			requestor.sendMessage("World is disabled");
			return;
		}

		log.debug(requestor + " performing copy");

		Block targetBlock = requestor.getTargetBlock(null, 100);

		Location location = parseSpecifiedLocation(requestor, args);

		Shortcut shortcut = playerSettings.getShortcut(shortcutName);
		InputStream in = null;

		try {
			in = new URL(shortcut.getUrl()).openStream();

			BufferedImage image = ImageIO.read(in);

			image = ImageUtil.scaleImage(image, playerSettings.getBuildWidth(), playerSettings.getBuildHeight());

			requestor.sendMessage("Copying your image: " + shortcut.getUrl());
			requestor.sendMessage("Width: " + image.getWidth() + " Height: " + image.getHeight());

			Matrix4d rotationMatrix = null;
			if (location == null) {
				location = new Location(requestor.getWorld(), targetBlock.getX(), targetBlock.getY(), targetBlock.getZ());
				rotationMatrix = MatrixUtil.calculateRotation(requestor.getLocation());
			} else {
				rotationMatrix = MatrixUtil.calculateRotation(location);
			}

			BlockProfileType blockProfile = configurationManager.getBlockProfile(playerSettings.getBlockProfile());
			Stack<RevertableBlock> undoBuffer = createUndoBuffer(requestor);
			ImageCopier mcGraphics2d = new ImageCopier(blockProfile, location, requestor.getWorld(), rotationMatrix);
			
			mcGraphics2d.draw(image, undoBuffer);

		} catch (MalformedURLException e) {
			requestor.sendMessage("Bad URL");
			log.error("Bad URL during copycat copy", e);
		} catch (IOException e) {
			requestor.sendMessage("Error reading shortcut");
			log.error("Error reading shortcut", e);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	private Location parseSpecifiedLocation(Player requestor, Queue<String> args) {

		if (args.size() >= 5) {

			// The user has specified position manually

			int x = Integer.parseInt(args.remove());
			int y = Integer.parseInt(args.remove());
			int z = Integer.parseInt(args.remove());
			int yaw = Integer.parseInt(args.remove());
			int pitch = Integer.parseInt(args.remove());

			Location specifiedLocation = new Location(requestor.getWorld(), x, y, z);
			specifiedLocation.setYaw(yaw);
			specifiedLocation.setPitch(pitch);

			return specifiedLocation;
		}

		return null;
	}

	private Stack<RevertableBlock> createUndoBuffer(Player player) {
		Stack<RevertableBlock> undoBuffer = new Stack<RevertableBlock>();
		HashMap<String, LinkedBlockingDeque<Stack<RevertableBlock>>> undoBuffers = getPlugin().getUndoBuffers();
		String playerName = player.getName();

		if (!undoBuffers.containsKey(playerName)) {
			undoBuffers.put(playerName, new LinkedBlockingDeque<Stack<RevertableBlock>>(Settings.MAX_GLOBAL_UNDO));
		}

		undoBuffers.get(playerName).push(undoBuffer);
		return undoBuffer;
	}

	@Override
	public String getFunction() {
		return FUNCTION_NAME;
	}

	@Override
	protected boolean isNeedsOpPermissions() {
		return false;
	}
}
