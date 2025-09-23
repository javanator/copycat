package org.bukkitmodders.copycat.commands;

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
import org.bukkitmodders.copycat.services.TextureMapProcessor;
import org.bukkitmodders.copycat.util.ImageUtil;
import org.bukkitmodders.copycat.util.MatrixUtil;
import org.joml.Matrix3d;
import org.joml.Matrix3f;
import org.joml.Matrix4d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

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
        sb.append("\nUNDO - Undoes a prior copy");
        sb.append("\n<IMAGE NAME> - Copies the image to the targeted block");
        sb.append("\nCOPY <IMAGE NAME> <X> <Y> <Z> <PITCH> <YAW> <WORLD> - Alternate copy method.");
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

            Shortcut shortcut = playerSettings.getShortcut(operation);
            Location location = null;
            if ("copy".equalsIgnoreCase(operation)) {
                location = parseSpecifiedLocation(sender, argsQueue);
                new CCCommand(plugin).asyncDownloadAndCopy(sender, shortcut, location);
                sender.sendMessage("Rendering your image from: " + shortcut.getUrl());
            } else if ("undo".equalsIgnoreCase(operation)) {
                boolean performUndo = performUndo(sender, sender.getName());

                if (performUndo) {
                    sender.sendMessage("Performed Undo");
                } else {
                    sender.sendMessage("Nothing to Undo");
                }
            } else if (shortcut != null && sender instanceof Player) {
                Player player = (Player) sender;
                Block b = player.getTargetBlock(null, 100);
                location = new Location(b.getWorld(), b.getX(), b.getY(), b.getZ());
                location.setYaw(player.getLocation().getYaw());
                location.setPitch(player.getLocation().getPitch());
                sender.sendMessage("Rendering your image from: " + shortcut.getUrl());
                asyncDownloadAndCopy(sender, shortcut, location);
            }

            return true;
        } catch (Exception e) {
            log.error("Something Unexpected Happened", e);
        }

        return false;
    }

    public void asyncDownloadAndCopy(final CommandSender sender, Shortcut shortcut, final Location location) {
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        scheduler.runTaskAsynchronously(plugin, new AsyncImageDownloadRunnable(sender, location, shortcut, plugin));
    }

    private Location parseSpecifiedLocation(CommandSender sender, Queue<String> args) {

        // The user has specified position manually

        float x = Float.parseFloat(args.poll());
        float y = Float.parseFloat(args.poll());
        float z = Float.parseFloat(args.poll());
        float pitch = Float.parseFloat(args.poll());
        float yaw = Float.parseFloat(args.poll());

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

    void performDraw(CommandSender sender, Location location, BufferedImage image) {
        ConfigurationManager configurationManager = plugin.getConfigurationManager();
        PlayerSettingsManager senderSettings = configurationManager.getPlayerSettings(sender.getName());
        Stack<RevertableBlock> undoBuffer = null;
        if (senderSettings.isUndoEnabled()) {
            undoBuffer = new Stack<RevertableBlock>();
        }

        try {
            BlockProfileType blockProfile = configurationManager.getBlockProfile(sender.getName());
            TextureMapProcessor textureMapProcessor = new TextureMapProcessor(blockProfile);

            IndexColorModel icm = ImageUtil.generateIndexColorModel(textureMapProcessor.getColorTable().keySet());

            image = ImageUtil.scaleImage(image, senderSettings.getBuildWidth(), senderSettings.getBuildHeight());

            if (senderSettings.isDithering()) {
                image = ImageUtil.ditherImage(image, icm);
            }
            Matrix4d rotationMatrix = null;

            rotationMatrix = MatrixUtil.calculateRotation(location);
            ImageCopier mcGraphics2d = new ImageCopier(textureMapProcessor, location, rotationMatrix);

            mcGraphics2d.draw(image, undoBuffer);

            sender.sendMessage("Scaled Width: " + image.getWidth() + " Scaled Height: " + image.getHeight());
            sender.sendMessage("Rendered to (X,Y,Z) PITCH YAW WORLD): (" + location.getX() + "," + location.getY() + "," + location.getZ() + ") " + location.getPitch() + " "
                    + location.getYaw() + " " + location.getWorld().getName());
            sender.sendMessage("Copycat Render complete");
        } finally {
            if (undoBuffer != null) {
                LinkedBlockingDeque<Stack<RevertableBlock>> undoBuffers = senderSettings.getUndoBuffer();
                undoBuffers.push(undoBuffer);
            }
        }
    }

    public boolean performUndo(CommandSender sender, String playerName) {
        PlayerSettingsManager playerSettings = plugin.getConfigurationManager().getPlayerSettings(playerName);

        LinkedBlockingDeque<Stack<RevertableBlock>> undoBuffer = playerSettings.getUndoBuffer();

        if (!undoBuffer.isEmpty()) {
            Stack<RevertableBlock> lastImageBlocks = undoBuffer.pop();

            while (!lastImageBlocks.isEmpty()) {
                lastImageBlocks.pop().revert();
            }

            return true;
        }

        return false;
    }

    /**
     * Purges the undo buffer of the target player.
     *
     * @param targetPlayer
     */
    public void purgeUndoBuffer(String targetPlayer) {
        PlayerSettingsManager playerSettings = plugin.getConfigurationManager().getPlayerSettings(targetPlayer);
        playerSettings.getUndoBuffer().clear();
    }
}
