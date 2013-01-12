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
import org.bukkit.inventory.ItemStack;
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

public class WandCommand implements CommandExecutor {
	static final Logger log = LoggerFactory.getLogger(WandCommand.class);
	final Nouveau plugin;

	public WandCommand(Nouveau nouveau) {
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
		sb.append("/" + getCommandString() + " [ ON | OFF | SET ]");
		sb.append("\nON - Enables image copy when the trigger is in the player's hand");
		sb.append("\nOFF - Disables image copy when the trigger is in the player's hand");
		sb.append("\nSET - Sets the wand item. Defaults to empty fist. ");
		sb.append("will render an image selected by the " + SetCommand.getCommandString() + " command.");

		Map<String, Object> desc = new LinkedHashMap<String, Object>();
		desc.put("description", "Magic wand mode commands");
		desc.put("usage", sb.toString());

		return desc;
	}

	public static Map<String, Object> getPermissions() {

		Map<String, Object> permissions = new LinkedHashMap<String, Object>();
		permissions.put("description", "Renders images in-game via command line");
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

			if (!sender.hasPermission(getPermissionNode())) {
				sender.sendMessage("You do not have permission");
			}

			ConfigurationManager configurationManager = plugin.getConfigurationManager();
			PlayerSettingsManager playerSettings = configurationManager.getPlayerSettings(sender.getName());

			if ("ON".equalsIgnoreCase(operation)) {

				playerSettings.setCopyEnabled(true);
				sender.sendMessage("Copying has been enabled for " + sender.getName());
			} else if ("OFF".equalsIgnoreCase(operation)) {

				playerSettings.setCopyEnabled(false);
				sender.sendMessage("Copying has been disabled for " + sender.getName());
			} else if ("SET".equalsIgnoreCase(operation)) {

				Player player = (Player) sender;
				String itemTrigger = player.getItemInHand().getType().name();
				playerSettings.setTrigger(itemTrigger);

				sender.sendMessage("Equipping: " + itemTrigger + " will trigger image copying when enabled");
			}

			return true;
		} catch (Exception e) {
			log.error("Something Unexpected Happened", e);
		}

		return false;
	}

	public void asyncDownloadAndCopy(final CommandSender sender, final Location location) {
		ConfigurationManager configurationManager = plugin.getConfigurationManager();
		final PlayerSettingsManager playerSettings = configurationManager.getPlayerSettings(sender.getName());

		BukkitScheduler scheduler = plugin.getServer().getScheduler();
		scheduler.runTaskAsynchronously(plugin, new AsyncImageDownloadRunnable(playerSettings, sender, location, plugin));
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
		Shortcut shortcut = senderSettings.getActiveShortcut();

		image = ImageUtil.scaleImage(image, senderSettings.getBuildWidth(), senderSettings.getBuildHeight());

		sender.sendMessage("Copying your image: " + shortcut.getUrl());
		sender.sendMessage("Native Width: " + image.getWidth() + "Native Height: " + image.getHeight());

		Matrix4d rotationMatrix = null;

		rotationMatrix = MatrixUtil.calculateRotation(location);

		BlockProfileType blockProfile = configurationManager.getBlockProfile(sender.getName());
		Stack<RevertableBlock> undoBuffer = new Stack<RevertableBlock>();
		LinkedBlockingDeque<Stack<RevertableBlock>> undoBuffers = senderSettings.getUndoBuffer();
		undoBuffers.add(undoBuffer);

		ImageCopier mcGraphics2d = new ImageCopier(blockProfile, location, rotationMatrix);

		mcGraphics2d.draw(image, undoBuffer);
	}
}
