package org.bukkitmodders.copycat.schema;

import java.util.ArrayList;
import java.util.List;

public class PlayerSettingsType {

    protected String blockProfile;
    protected int buildWidth;
    protected int buildHeight;
    protected String activeShortcut;
    protected String stampItem;
    protected boolean undoEnabled;
    protected boolean dithering;
    protected PlayerSettingsType.Shortcuts shortcuts;
    protected String playerName;
    protected boolean stampActivated;

    public String getBlockProfile() {
        return blockProfile;
    }

    public void setBlockProfile(String value) {
        this.blockProfile = value;
    }

    public int getBuildWidth() {
        return buildWidth;
    }

    public void setBuildWidth(int value) {
        this.buildWidth = value;
    }

    public int getBuildHeight() {
        return buildHeight;
    }

    public void setBuildHeight(int value) {
        this.buildHeight = value;
    }

    public String getActiveShortcut() {
        return activeShortcut;
    }

    public void setActiveShortcut(String value) {
        this.activeShortcut = value;
    }

    public String getStampItem() {
        return stampItem;
    }

    public void setStampItem(String value) {
        this.stampItem = value;
    }

    public boolean isUndoEnabled() {
        return undoEnabled;
    }

    public void setUndoEnabled(boolean value) {
        this.undoEnabled = value;
    }

    public boolean isDithering() {
        return dithering;
    }

    public void setDithering(boolean value) {
        this.dithering = value;
    }

    public Shortcuts getShortcuts() {
        return shortcuts;
    }

    public void setShortcuts(Shortcuts value) {
        this.shortcuts = value;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String value) {
        this.playerName = value;
    }

    public boolean isStampActivated() {
        return stampActivated;
    }

    public void setStampActivated(boolean value) {
        this.stampActivated = value;
    }


    public static class Shortcuts {

        protected List<Shortcut> shortcut;

        public List<Shortcut> getShortcut() {
            if (shortcut == null) {
                shortcut = new ArrayList<Shortcut>();
            }
            return this.shortcut;
        }


        public static class Shortcut {

            protected String name;
            protected String url;

            public String getName() {
                return name;
            }

            public void setName(String value) {
                this.name = value;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String value) {
                this.url = value;
            }

        }

    }

}
