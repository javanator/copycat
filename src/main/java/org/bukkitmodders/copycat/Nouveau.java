package org.bukkitmodders.copycat;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkitmodders.copycat.commands.*;
import org.bukkitmodders.copycat.managers.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Nouveau extends JavaPlugin {

    private static Logger log = LoggerFactory.getLogger(Nouveau.class);
    private static final String DATAFILE = "pluginSettings.xml";
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
        // Check if commands exist before setting executors
        var imgCommand = getCommand(ImgCommand.getCommandString());
        if (imgCommand != null) {
            imgCommand.setExecutor(new ImgCommand(this));
        } else {
            log.warn("Command '" + ImgCommand.getCommandString() + "' not found in plugin.yml");
        }

        var setCommand = getCommand(SetCommand.getCommandString());
        if (setCommand != null) {
            setCommand.setExecutor(new SetCommand(this));
        } else {
            log.warn("Command '" + SetCommand.getCommandString() + "' not found in plugin.yml");
        }

        var adminCommand = getCommand(AdminCommand.getCommandString());
        if (adminCommand != null) {
            adminCommand.setExecutor(new AdminCommand(this));
        } else {
            log.warn("Command '" + AdminCommand.getCommandString() + "' not found in plugin.yml");
        }

        var stampCommand = getCommand(StampCommand.getCommandString());
        if (stampCommand != null) {
            stampCommand.setExecutor(new StampCommand(this));
        } else {
            log.warn("Command '" + StampCommand.getCommandString() + "' not found in plugin.yml");
        }

        var ccCommand = getCommand(CCCommand.getCommandString());
        if (ccCommand != null) {
            ccCommand.setExecutor(new CCCommand(this));
        } else {
            log.warn("Command '" + CCCommand.getCommandString() + "' not found in plugin.yml");
        }

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
