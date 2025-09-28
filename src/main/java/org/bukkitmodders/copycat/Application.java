package org.bukkitmodders.copycat;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkitmodders.copycat.commands.CommandBuilders;
import org.bukkitmodders.copycat.managers.ConfigurationManager;
import org.bukkitmodders.copycat.managers.PlayerSettingsManager;
import org.bukkitmodders.copycat.model.BlockProfileType;
import org.bukkitmodders.copycat.model.RevertibleBlock;
import org.bukkitmodders.copycat.services.ImageCopier;
import org.bukkitmodders.copycat.services.TextureMapProcessor;
import org.bukkitmodders.copycat.util.ImageUtil;
import org.bukkitmodders.copycat.util.MatrixUtil;
import org.joml.Matrix4d;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Logger;

public class Application extends JavaPlugin {

    private static final String DATAFILE = "pluginSettings.json";
    private static Application INSTANCE;
    private static Logger LOG;
    private ConfigurationManager configurationManager;
    private CommandBuilders commandBuilders = new CommandBuilders();


    @Override
    public void onDisable() {

    }

    @Override
    public void onEnable() {
        Application.INSTANCE = this;
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, command -> {

            command.registrar().register(getCommandTreeBuilder().build());
        });
    }

    private LiteralArgumentBuilder<CommandSourceStack> getCommandTreeBuilder() {
        return Commands.literal("copycat")
                .then(commandBuilders.buildAdminCommand())
                .then(commandBuilders.buildListCommand())
                .then(commandBuilders.buildAddCommand())
                .then(commandBuilders.buildRemoveCommand())
                .then(commandBuilders.buildCopyCommand())
                .then(commandBuilders.buildPollCommand())
                .then(commandBuilders.buildSetCommand());
    }


    public ConfigurationManager getConfigurationManager() {

        if (this.configurationManager == null) {
            String file = getDataFolder().getAbsolutePath() + File.separatorChar + DATAFILE;
            this.configurationManager = new ConfigurationManager(file);
        }

        return this.configurationManager;
    }

    public static Application getInstance() {
        return INSTANCE;
    }

    public boolean performUndo(CommandSender sender, String playerName) {
        PlayerSettingsManager playerSettings = getConfigurationManager().getPlayerSettings(playerName);

        LinkedBlockingDeque<Stack<RevertibleBlock>> undoBuffer = playerSettings.getUndoBuffer();

        if (!undoBuffer.isEmpty()) {
            Stack<RevertibleBlock> lastImageBlocks = undoBuffer.pop();

            while (!lastImageBlocks.isEmpty()) {
                lastImageBlocks.pop().revert();
            }

            return true;
        }

        return false;
    }
}
