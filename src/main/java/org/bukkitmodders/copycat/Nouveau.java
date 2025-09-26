package org.bukkitmodders.copycat;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkitmodders.copycat.commands.*;
import org.bukkitmodders.copycat.managers.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Nouveau extends JavaPlugin {

    private static Logger log = LoggerFactory.getLogger(Nouveau.class);
    private static final String DATAFILE = "pluginSettings.json";
    private ConfigurationManager configurationManager;

    public void onDisable() {

        getCommand(ImgCommand.getCommandString()).setExecutor(null);
        getCommand(SetCommand.getCommandString()).setExecutor(null);
        getCommand(AdminCommand.getCommandString()).setExecutor(null);
        getCommand(StampCommand.getCommandString()).setExecutor(null);
        getCommand(CCCommand.getCommandString()).setExecutor(null);

        log.info(getDescription().getName() + " " + getDescription().getVersion() + " Disabled");
    }

    public void onEnable() {
        var imgCommand = getCommand(ImgCommand.getCommandString());
        imgCommand.setExecutor(new ImgCommand(this));

        var setCommand = getCommand(SetCommand.getCommandString());
        setCommand.setExecutor(new SetCommand(this));

        var adminCommand = getCommand(AdminCommand.getCommandString());
        adminCommand.setExecutor(new AdminCommand(this));

        var stampCommand = getCommand(StampCommand.getCommandString());
        stampCommand.setExecutor(new StampCommand(this));

        var ccCommand = getCommand(CCCommand.getCommandString());
        ccCommand.setExecutor(new CCCommand(this));

        getServer().getPluginManager().registerEvents(new StampListener(this), this);

        ConfigurationManager cm = getConfigurationManager();
        cm.updateDefaultBlockProfile(ConfigurationManager.generateDefaultBlockProfile());

        log.info(getDescription().getName() + " " + getDescription().getVersion() + " Enabled");
    }

    public ConfigurationManager getConfigurationManager() {

        if (this.configurationManager == null) {

            String file = getDataFolder().getAbsolutePath() + File.separatorChar + DATAFILE;

            this.configurationManager = new ConfigurationManager(file);
        }

        return this.configurationManager;
    }
}
