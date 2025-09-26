package org.bukkitmodders.copycat.model;

import java.util.ArrayList;
import java.util.List;


public class BlockProfileType {

    protected List<Block> block;
    protected String name;

    public List<Block> getBlock() {
        if (block == null) {
            block = new ArrayList<Block>();
        }
        return this.block;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }


    public static class Block {

        protected String name;
        protected int textureIndex;

        public String getName() {
            return name;
        }

        public void setName(String value) {
            this.name = value;
        }

        public int getTextureIndex() {
            return textureIndex;
        }

        public void setTextureIndex(int value) {
            this.textureIndex = value;
        }

    }
}
