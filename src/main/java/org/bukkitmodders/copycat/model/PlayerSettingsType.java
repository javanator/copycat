package org.bukkitmodders.copycat.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class PlayerSettingsType {

    protected String blockProfile;
    protected int buildWidth;
    protected int buildHeight;
    protected String activeShortcut;
    protected String stampItem;
    protected boolean undoEnabled;
    protected boolean dithering;
    protected List<Shortcut> shortcuts = new ArrayList<>();
    protected String playerName;
    protected boolean stampActivated;


    @Getter
    @Setter
    public static class Shortcut {
        protected String name;
        protected String url;
    }
}
