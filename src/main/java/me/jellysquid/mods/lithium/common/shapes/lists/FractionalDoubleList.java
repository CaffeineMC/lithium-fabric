package me.jellysquid.mods.lithium.common.shapes.lists;

import it.unimi.dsi.fastutil.doubles.AbstractDoubleList;

public class FractionalDoubleList extends AbstractDoubleList {
    private final int sectionCount;
    private final double scale;

    public FractionalDoubleList(int sectionCount) {
        this.sectionCount = sectionCount;
        this.scale = 1.0D / sectionCount;
    }

    @Override
    public double getDouble(int position) {
        return position * this.scale;
    }

    @Override
    public int size() {
        return this.sectionCount + 1;
    }
}
