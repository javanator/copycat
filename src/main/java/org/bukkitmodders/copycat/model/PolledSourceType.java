package org.bukkitmodders.copycat.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PolledSourceType {
    protected String shortcutNameRef;
    protected int refreshRateMilliseconds;
    protected long worldX;
    protected long worldY;
    protected double yaw;
}

