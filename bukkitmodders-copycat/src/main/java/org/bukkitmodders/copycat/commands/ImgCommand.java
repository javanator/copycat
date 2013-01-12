package org.bukkitmodders.copycat.commands;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingDeque;

import javax.imageio.ImageIO;
import javax.vecmath.Matrix4d;

import org.apache.commons.io.IOUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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

public class ImgCommand implements CommandExecutor {
	private static final Logger log = LoggerFactory.getLogger(ImgCommand.class);
	private final Nouveau plugin;

	public ImgCommand(Nouveau nouveau) {
		this.plugin = nouveau;
	}

	public static String getCommandString() {
		return "ccimg";
	}

	/**
	 * Used during build time to generate the plugin.yml file. Not used during
	 * runtime.
	 * 
	 * @return
	 */
	public static Map<String, Object> getDescription() {

		StringBuffer sb = new StringBuffer();
		sb.append("/" + getCommandString() + " [ ADD | DEL | LIST | CLEAN | COPY ]");
		sb.append("\nADD <name>=<url> - Add an image URL");
		sb.append("\nDEL <name>- Deletes an image URL by name");
		sb.append("\nLIST - Displays a list of your images");
		sb.append("\nCLEAN - Automatically cleans invalid images");
		sb.append("\nCOPY - Alternate copy method. Specify a location in the form of X Y Z PITCH YAW WORLDNAME");

		Map<String, Object> desc = new LinkedHashMap<String, Object>();
		desc.put("description", "Commands for various image operations");
		desc.put("usage", sb.toString());

		return desc;
	}

	public static Map<String, Object> getPermissions() {

		Map<String, Object> permissions = new LinkedHashMap<String, Object>();
		permissions.put("description", "Image management functions");
		permissions.put("default", "true");

		return permissions;
	}

	public static String getPermissionNode() {
		return "copycat.ccimg";
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
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

			if ("add".equalsIgnoreCase(operation)) {

				String imageName = argsQueue.poll();
				String imageUrl = argsQueue.poll();

				if (imageName == null) {
					sender.sendMessage("No image name specified");
				}

				if (imageUrl == null) {
					sender.sendMessage("No image URL specified");
				}

				playerSettings.addShortcut(imageName, imageUrl);
				sender.sendMessage(imageName + " added");
			} else if ("del".equalsIgnoreCase(operation)) {

				String imageName = argsQueue.poll();
				if (imageName == null) {
					sender.sendMessage("No image name specified");
				}

				playerSettings.deleteShortcut(imageName);
			} else if ("clean".equalsIgnoreCase(operation)) {
				sender.sendMessage("Cleaning up your URLs. Removing bad URLs and non-images");
				playerSettings.cleanShortcuts();
				sender.sendMessage("Done with URL cleanup");
			} else if ("list".equalsIgnoreCase(operation)) {
				playerSettings.tellShortcuts(sender);
			} else if ("copy".equalsIgnoreCase(operation)) {
				Location location = parseSpecifiedLocation(plugin.getServer().getWorld("default"), argsQueue);
				performDraw(sender, location);
			}

			return true;
		} catch (Exception e) {
			log.error("Something Unexpected Happened", e);
		}

		return false;
	}

	private Location parseSpecifiedLocation(World world, Queue<String> args) {

		if (args.size() >= 5) {

			// The user has specified position manually

			int x = Integer.parseInt(args.remove());
			int y = Integer.parseInt(args.remove());
			int z = Integer.parseInt(args.remove());
			int yaw = Integer.parseInt(args.remove());
			int pitch = Integer.parseInt(args.remove());

			Location specifiedLocation = new Location(world, x, y, z);
			specifiedLocation.setYaw(yaw);
			specifiedLocation.setPitch(pitch);

			return specifiedLocation;
		}

		return null;
	}

	public void performDraw(CommandSender sender, Location location) {

		ConfigurationManager configurationManager = plugin.getConfigurationManager();
		PlayerSettingsManager senderSettings = configurationManager.getPlayerSettings(sender.getName());
		Shortcut shortcut = senderSettings.getActiveShortcut();

		InputStream in = null;

		try {
			in = new URL(shortcut.getUrl()).openStream();

			BufferedImage image = ImageIO.read(in);

			image = ImageUtil.scaleImage(image, senderSettings.getBuildWidth(), senderSettings.getBuildHeight());

			sender.sendMessage("Copying your image: " + shortcut.getUrl());
			sender.sendMessage("Native Width: " + image.getWidth() + "Native Height: " + image.getHeight());

			Matrix4d rotationMatrix = null;

			if (location == null && sender instanceof Player) {
				Player player = (Player) sender;
				Block targetBlock = player.getTargetBlock(null, 100);
				location = new Location(player.getWorld(), targetBlock.getX(), targetBlock.getY(), targetBlock.getZ());
				rotationMatrix = MatrixUtil.calculateRotation(player.getLocation());
			} else {
				rotationMatrix = MatrixUtil.calculateRotation(location);
			}

			BlockProfileType blockProfile = configurationManager.getBlockProfile(sender.getName());
			Stack<RevertableBlock> undoBuffer = new Stack<RevertableBlock>();
			LinkedBlockingDeque<Stack<RevertableBlock>> undoBuffers = senderSettings.getUndoBuffer();
			undoBuffers.add(undoBuffer);

			ImageCopier mcGraphics2d = new ImageCopier(blockProfile, location, rotationMatrix);

			mcGraphics2d.draw(image, undoBuffer);

		} catch (MalformedURLException ioe) {
			sender.sendMessage("Bad URL " + ioe.getMessage());
		} catch (IOException ioe) {
			sender.sendMessage("Error reading shortcut");
			log.error("Error reading shortcut", ioe);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}
}
