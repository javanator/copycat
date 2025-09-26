package org.bukkitmodders.copycat.model;

public class ObjectFactory {


    public ObjectFactory() {
    }

    public BlockProfileType createBlockProfileType() {
        return new BlockProfileType();
    }

    public BlockProfileType.Block createBlockProfileTypeBlock() {
        return new BlockProfileType.Block();
    }

    public PlayerSettingsType.Shortcuts.Shortcut createPlayerSettingsTypeShortcutsShortcut() {
        return new PlayerSettingsType.Shortcuts.Shortcut();
    }

    public PlayerSettingsType.Shortcuts createPlayerSettingsTypeShortcuts() {
        return new PlayerSettingsType.Shortcuts();
    }

    public GlobalSettingsType createGlobalSettingsType() {
        return new GlobalSettingsType();
    }

    public PluginConfig createPluginConfig() {
        return new PluginConfig();
    }

    public GlobalSettingsType.BlockProfiles createGlobalSettingsTypeBlockProfiles() {
        return new GlobalSettingsType.BlockProfiles();
    }

    public PlayerSettingsType createPlayerSettingsType() {
        return new PlayerSettingsType();
    }

    public PluginConfig.Preferences createPluginConfigPreferences() {
        return new PluginConfig.Preferences();
    }
}
