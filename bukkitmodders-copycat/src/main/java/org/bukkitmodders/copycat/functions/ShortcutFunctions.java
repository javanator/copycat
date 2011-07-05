package org.bukkitmodders.copycat.functions;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Queue;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.vecmath.Matrix4d;

import org.apache.commons.io.IOUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkitmodders.copycat.Plugin;
import org.bukkitmodders.copycat.RevertableBlock;
import org.bukkitmodders.copycat.managers.ConfigurationManager;
import org.bukkitmodders.copycat.managers.PlayerSettingsManager;
import org.bukkitmodders.copycat.schema.BlockProfileType;
import org.bukkitmodders.copycat.schema.PlayerSettingsType.Shortcuts.Shortcut;
import org.bukkitmodders.copycat.util.ImageCopier;
import org.bukkitmodders.copycat.util.ImageUtil;
import org.bukkitmodders.copycat.util.NeedMoreArgumentsException;
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
		sb.append(getOperationPrefix() + "copy <shortcut name> - Copies Image\n");
		sb.append(getOperationPrefix() + "add <shortcut name> <image url> - Add shortcut\n");
		sb.append(getOperationPrefix() + "delete <shortcut name> - Deletes shortcut\n");
		sb.append(getOperationPrefix() + "list - Lists shortcuts\n");
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

		PlayerSettingsManager playerSettings = getPlugin().getConfigurationManager().getPlayerSettings(
				requestor.getName());

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

		Location location = null;
		Matrix4d orientationMatrix = null;
		if (args.size() < 5) {

			// The user has specified position manually

			int x = Integer.parseInt(args.remove());
			int y = Integer.parseInt(args.remove());
			int z = Integer.parseInt(args.remove());

			location = new Location(requestor.getWorld(), x, y, z);
			orientationMatrix = new Matrix4d();
			//TODO: parse an orientation
		}

		Shortcut shortcut = playerSettings.getShortcut(shortcutName);
		InputStream in = null;

		try {
			in = new URL(shortcut.getUrl()).openStream();

			BufferedImage image = ImageIO.read(in);

			image = ImageUtil.scaleImage(image, playerSettings.getBuildWidth(), playerSettings.getBuildHeight());

			requestor.sendMessage("Copying your image: " + shortcut.getUrl());
			requestor.sendMessage("Width: " + image.getWidth() + " Height: " + image.getHeight());
			Stack<RevertableBlock> undoBuffer = createUndoBuffer(requestor);

			if (location == null) {
				location = new Location(requestor.getWorld(), targetBlock.getX(), targetBlock.getY(),
						targetBlock.getZ());
				orientationMatrix = calculateOrientation(requestor, targetBlock);
			}

			BlockProfileType blockProfile = configurationManager.getBlockProfile(playerSettings.getBlockProfile());
			ImageCopier mcGraphics2d = new ImageCopier(blockProfile, location, requestor.getWorld(), orientationMatrix);
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

	private Stack<RevertableBlock> createUndoBuffer(Player player) {
		Stack<RevertableBlock> undoBuffer = new Stack<RevertableBlock>();
		HashMap<String, Stack<Stack<RevertableBlock>>> undoBuffers = getPlugin().getUndoBuffers();
		String playerName = player.getName();

		if (!undoBuffers.containsKey(playerName)) {
			undoBuffers.put(playerName, new Stack<Stack<RevertableBlock>>());
		}

		undoBuffers.get(playerName).push(undoBuffer);
		return undoBuffer;
	}

	private Matrix4d calculateOrientation(Player player, Block block) {

		Matrix4d orientation = new Matrix4d();
		orientation.setIdentity();
		Matrix4d rotXZ = new Matrix4d();
		orientation.setIdentity();

		Location location = player.getLocation();

		double yaw = Math.abs(location.getYaw());

		yaw %= 360;

		double baseAngle = 0;
		int sign = (location.getYaw() < 0) ? -1 : 1;

		if (yaw > (90 - 45) & yaw < (90 + 45)) {
			baseAngle = 90;
		} else if (yaw > (180 - 45) & yaw < (180 + 45)) {
			baseAngle = 180;
		} else if (yaw > (270 - 45) && yaw < (270 + 45)) {
			baseAngle = 270;
		}

		if (sign < 0) {
			baseAngle = 360 - baseAngle;
		}

		double pitch = location.getPitch();
		boolean down = false;
		boolean up = false;
		// if (pitch > 45) {
		// down = true;
		// } else if (pitch < -45) {
		// up = true;
		// }

		if (baseAngle == 0 || baseAngle == 360) {
			if (down || up) {
				rotXZ.rotX(Math.toRadians(90));
				orientation.mul(rotXZ);
			} else {
				rotXZ.rotX(Math.toRadians(+90));
				orientation.mul(rotXZ);
				rotXZ.rotZ(Math.toRadians(-180));
				orientation.mul(rotXZ);
				rotXZ.rotX(Math.toRadians(-90));
				orientation.mul(rotXZ);
			}
		} else if (baseAngle == 90) {
			if (down || up) {
				rotXZ.rotZ(Math.toRadians(-90));
				orientation.mul(rotXZ);
				rotXZ.rotX(Math.toRadians(90));
				orientation.mul(rotXZ);
			} else {
				rotXZ.rotX(Math.toRadians(90));
				orientation.mul(rotXZ);
				rotXZ.rotZ(Math.toRadians(-90));
				orientation.mul(rotXZ);
				rotXZ.rotX(Math.toRadians(-90));
				orientation.mul(rotXZ);
			}
		} else if (baseAngle == 180) {
			if (down || up) {
				rotXZ.rotX(Math.toRadians(-90));
			}
		} else if (baseAngle == 270) {
			if (down || up) {
				rotXZ.rotZ(Math.toRadians(90));
				orientation.mul(rotXZ);
				rotXZ.rotX(Math.toRadians(-90));
				orientation.mul(rotXZ);
			} else {
				rotXZ.rotX(Math.toRadians(-90));
				orientation.mul(rotXZ);
				rotXZ.rotZ(Math.toRadians(-90));
				orientation.mul(rotXZ);
				rotXZ.rotX(Math.toRadians(90));
				orientation.mul(rotXZ);
			}
		}

		return orientation;
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
