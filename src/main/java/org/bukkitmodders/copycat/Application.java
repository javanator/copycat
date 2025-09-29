package org.bukkitmodders.copycat;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkitmodders.copycat.commands.CommandBuilder;
import org.bukkitmodders.copycat.managers.ConfigurationManager;

import java.io.File;
import java.util.logging.Logger;

public class Application extends JavaPlugin {

    private static final String DATAFILE = "pluginSettings.json";
    private static Application INSTANCE;
    private static Logger LOG;
    private final CommandBuilder commandBuilder = new CommandBuilder(this);
    private ConfigurationManager configurationManager;

    @Override
    public void onEnable() {
        Application.INSTANCE = this;

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, command -> {

            command.registrar().register(getCommandTreeBuilder().build());
        });
    }

    private LiteralArgumentBuilder<CommandSourceStack> getCommandTreeBuilder() {
        return Commands.literal("cc")
                .then(commandBuilder.buildAdminCommand())
                .then(commandBuilder.buildListCommand())
                .then(commandBuilder.buildAddCommand())
                .then(commandBuilder.buildRemoveCommand())
                .then(commandBuilder.buildCopyCommand())
                .then(commandBuilder.buildUndoCommand())
                .then(commandBuilder.buildPollCommand())
                .then(commandBuilder.buildSetCommand());
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

}
