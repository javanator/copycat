package org.bukkitmodders.copycat.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class BlockProfileType {

    protected List<Block> block=new ArrayList<>();
    protected String name;

    @Setter
    @Getter
    public static class Block {
        protected String name;
        protected int textureIndex;
    }
}
