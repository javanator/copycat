package org.bukkitmodders.copycat.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class GlobalSettingsType {

    protected int maxImageWidth;
    protected int maxImageHeight;
    List<BlockProfileType> blockProfiles = new ArrayList<>();
}
