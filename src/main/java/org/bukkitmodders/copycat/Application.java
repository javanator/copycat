package org.bukkitmodders.copycat;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.sun.jna.NativeLibrary;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkitmodders.copycat.commands.CommandBuilder;
import org.bukkitmodders.copycat.listener.PlayerListener;
import org.bukkitmodders.copycat.managers.ConfigurationManager;
import org.bukkitmodders.copycat.managers.PlayerSettingsManager;
import org.bukkitmodders.copycat.managers.UndoBufferManager;
import org.bukkitmodders.copycat.services.MediaService;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import java.io.File;

public class Application extends JavaPlugin {

    private static final String DATAFILE = "pluginSettings.json";
    private static Application INSTANCE;
    @Getter
    private final MediaService mediaService = new MediaService(this);
    private final CommandBuilder commandBuilder = new CommandBuilder(this, mediaService);
    @Getter
    private final UndoBufferManager undoBufferManager = new UndoBufferManager(this);
    private ConfigurationManager configurationManager;

    @Override
    public void onEnable() {
        Application.INSTANCE = this;
        NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "path/to/vlc");
        boolean found = new NativeDiscovery().discover();
                if (!found) {
            getLogger().info("LibVLC not found!");
            getLogger().info("Please ensure VLC is installed for video support.");

        } else {
            getLogger().info("LibVLC found and loaded.");
        }

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, command -> {
            command.registrar().register(getCommandTreeBuilder().build());
        });
    }

    @Override
    public void onDisable() {
        mediaService.stop();
    }

    private LiteralArgumentBuilder<CommandSourceStack> getCommandTreeBuilder() {
        return Commands.literal("cc")
                .then(commandBuilder.buildAdminCommand())
                .then(commandBuilder.buildListCommand())
                .then(commandBuilder.buildAddCommand())
                .then(commandBuilder.buildRemoveCommand())
                .then(commandBuilder.buildCopyCommand())
                .then(commandBuilder.buildUndoCommand())
                .then(commandBuilder.buildStopCommand())
                .then(commandBuilder.buildSetCommand());
    }

    public ConfigurationManager getConfigurationManager() {
        if (this.configurationManager == null) {
            String file = getDataFolder().getAbsolutePath() + File.separatorChar + DATAFILE;
            this.configurationManager = new ConfigurationManager(file, this);
        }
        return this.configurationManager;
    }

    /**
     * Convenience method
     *
     * @param playerName
     * @return
     */
    public PlayerSettingsManager getPlayerSettings(String playerName) {
        return getConfigurationManager().getPlayerSettings(playerName);
    }

    public static Application getInstance() {
        return INSTANCE;
    }

}
