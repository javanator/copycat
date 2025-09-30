package org.bukkitmodders.copycat.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.awt.image.BufferedImage;

@Builder(setterPrefix = "with")
@Getter
@Setter
public class BuildContext {
    private Player player;
    private BlockProfileType blockProfile;
    private Location location;
    private BufferedImage image;
    private PlayerSettingsType.Shortcut shortcut;
}
