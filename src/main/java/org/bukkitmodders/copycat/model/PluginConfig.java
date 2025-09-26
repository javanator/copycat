package org.bukkitmodders.copycat.model;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;


public class PluginConfig {

    @JsonProperty("globalSettings")
    private GlobalSettingsType globalSettings;

    @JsonProperty("preferences")
    private Preferences preferences;

    public Preferences getPreferences() {
        return preferences;
    }

    public void setPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

    public GlobalSettingsType getGlobalSettings() {
        return globalSettings;
    }

    public void setGlobalSettings(GlobalSettingsType globalSettings) {
        this.globalSettings = globalSettings;
    }

    public static class Preferences {

        protected List<PlayerSettingsType> playerPreferences;

        public List<PlayerSettingsType> getPlayerPreferences() {
            if (playerPreferences == null) {
                playerPreferences = new ArrayList<PlayerSettingsType>();
            }
            return this.playerPreferences;
        }

    }

}
