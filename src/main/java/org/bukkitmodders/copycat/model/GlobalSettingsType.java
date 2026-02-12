package org.bukkitmodders.copycat.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class GlobalSettingsType {

    protected boolean pollOnPlayerConnectedOnly;
    protected int pollingRefresh;
    protected int maxImageWidth;
    protected int maxImageHeight;
    protected int undoBufferLimit;
    protected List<BlockProfileType> blockProfiles = new ArrayList<>();
}
