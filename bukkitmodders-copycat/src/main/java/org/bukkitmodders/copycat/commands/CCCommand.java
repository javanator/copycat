package org.bukkitmodders.copycat.commands;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingDeque;

import javax.vecmath.Matrix4d;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkitmodders.copycat.Nouveau;
import org.bukkitmodders.copycat.managers.ConfigurationManager;
import org.bukkitmodders.copycat.managers.PlayerSettingsManager;
import org.bukkitmodders.copycat.plugin.RevertableBlock;
import org.bukkitmodders.copycat.schema.BlockProfileType;
import org.bukkitmodders.copycat.schema.PlayerSettingsType.Shortcuts.Shortcut;
import org.bukkitmodders.copycat.services.ImageCopier;
import org.bukkitmodders.copycat.util.ImageUtil;
import org.bukkitmodders.copycat.util.MatrixUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CCCommand implements CommandExecutor {
	static final Logger log = LoggerFactory.getLogger(CCCommand.class);
	final Nouveau plugin;

	public CCCommand(Nouveau nouveau) {
		this.plugin = nouveau;
	}

	public static String getCommandString() {
		return "cc";
	}

	/**
	 * Used during build time to generate the plugin.yml file. Not used during
	 * runtime.
	 * 
	 * @return
	 */
	public static Map<String, Object> getDescription() {

		StringBuffer sb = new StringBuffer();
		sb.append("/" + getCommandString() + " <IMAGE NAME>");
		sb.append("\n<IMAGE NAME> - Copies the image to the targeted block");
		sb.append("\nCOPY - Alternate copy method. ");
		sb.append("Specify a location in the form of <IMAGE NAME> [ <X> <Y> <Z> <PITCH> <YAW> <WORLD> ].");
		sb.append(" Provided for integration potential. Most users should not use this.");

		Map<String, Object> desc = new LinkedHashMap<String, Object>();
		desc.put("description", "Image rendering commands");
		desc.put("usage", sb.toString());

		return desc;
	}

	public static Map<String, Object> getPermissions() {

		Map<String, Object> permissions = new LinkedHashMap<String, Object>();
		permissions.put("description", "Renders images in-game via commands. Must already have permission.build");
		permissions.put("default", "true");

		return permissions;
	}

	public static String getPermissionNode() {
		return "copycat.cc";
	}

	@Override
	public boolean onCommand(final CommandSender sender, Command command, String alias, String[] args) {
		try {
			Queue<String> argsQueue = new LinkedList<String>();
			argsQueue.addAll(Arrays.asList(args));
			String operation = argsQueue.poll();

			if (operation == null) {
				return false;
			}

			boolean isBuildSet = sender.isPermissionSet("permissions.build");
			boolean isBuilder = sender.hasPermission("permissions.build");

			if (isBuildSet && !isBuilder) {
				sender.sendMessage("You do not have permissions.build ");
			} else if (!sender.hasPermission(getPermissionNode())) {
				sender.sendMessage("You do not have " + getPermissionNode());
			}

			ConfigurationManager configurationManager = plugin.getConfigurationManager();
			PlayerSettingsManager playerSettings = configurationManager.getPlayerSettings(sender.getName());

			if ("copy".equalsIgnoreCase(operation)) {
				// FIXME
				Location location = parseSpecifiedLocation(sender, argsQueue);
				Shortcut shortcut = playerSettings.getShortcut(operation);
				sender.sendMessage("Rendering your image from: " + playerSettings.getActiveShortcut().getUrl());
				asyncDownloadAndCopy(sender, shortcut, location);
			} else if (playerSettings.getShortcut(operation) != null && sender instanceof Player) {
				Player player = (Player) sender;
				Block b = player.getTargetBlock(null, 100);
				Location location = new Location(b.getWorld(), b.getX(), b.getY(), b.getZ());
				location.setYaw(player.getLocation().getYaw());
				location.setPitch(player.getLocation().getPitch());
				player.sendMessage("Rendering your image from: " + playerSettings.getActiveShortcut().getUrl());
				asyncDownloadAndCopy(player, playerSettings.getShortcut(operation), location);
			}

			return true;
		} catch (Exception e) {
			log.error("Something Unexpected Happened", e);
		}

		return false;
	}

	public void asyncDownloadAndCopy(final CommandSender sender, Shortcut shortcut, final Location location) {
		ConfigurationManager configurationManager = plugin.getConfigurationManager();
		BukkitScheduler scheduler = plugin.getServer().getScheduler();
		scheduler.runTaskAsynchronously(plugin, new AsyncImageDownloadRunnable(sender, location, shortcut, plugin));
	}

	private Location parseSpecifiedLocation(CommandSender sender, Queue<String> args) {

		if (args.size() >= 5) {

			// The user has specified position manually

			int x = Integer.parseInt(args.poll());
			int y = Integer.parseInt(args.poll());
			int z = Integer.parseInt(args.poll());
			int yaw = Integer.parseInt(args.poll());
			int pitch = Integer.parseInt(args.poll());
			String worldStr = args.poll();

			World world = null;
			if (!StringUtils.isBlank(worldStr)) {
				world = plugin.getServer().getWorld(worldStr);
			} else if (sender instanceof Player) {
				world = ((Player) sender).getWorld();
			}

			Location specifiedLocation = new Location(world, x, y, z);
			specifiedLocation.setYaw(yaw);
			specifiedLocation.setPitch(pitch);

			return specifiedLocation;
		}

		return null;
	}

	void performDraw(CommandSender sender, Location location, BufferedImage image) {

		ConfigurationManager configurationManager = plugin.getConfigurationManager();
		PlayerSettingsManager senderSettings = configurationManager.getPlayerSettings(sender.getName());
		image = ImageUtil.scaleImage(image, senderSettings.getBuildWidth(), senderSettings.getBuildHeight());

		Matrix4d rotationMatrix = null;

		rotationMatrix = MatrixUtil.calculateRotation(location);

		BlockProfileType blockProfile = configurationManager.getBlockProfile(sender.getName());
		Stack<RevertableBlock> undoBuffer = new Stack<RevertableBlock>();
		LinkedBlockingDeque<Stack<RevertableBlock>> undoBuffers = senderSettings.getUndoBuffer();
		undoBuffers.add(undoBuffer);

		ImageCopier mcGraphics2d = new ImageCopier(blockProfile, location, rotationMatrix);

		mcGraphics2d.draw(image, undoBuffer);

		sender.sendMessage("Scaled Width: " + image.getWidth() + " Scaled Height: " + image.getHeight());
		sender.sendMessage("Copycat Render complete");
	}
}
