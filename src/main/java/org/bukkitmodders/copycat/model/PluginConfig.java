package org.bukkitmodders.copycat.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Setter
@Getter
public class PluginConfig {

    @JsonProperty("globalSettings")
    private GlobalSettingsType globalSettings;

    @JsonProperty("preferences")
    private Preferences preferences;

    @Setter
    @Getter
    public static class Preferences {
        protected List<PlayerSettingsType> playerPreferences;
    }
}
