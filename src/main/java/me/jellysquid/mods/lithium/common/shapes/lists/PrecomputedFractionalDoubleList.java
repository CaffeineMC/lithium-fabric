package me.jellysquid.mods.lithium.common.shapes.lists;

import it.unimi.dsi.fastutil.doubles.AbstractDoubleList;


public class PrecomputedFractionalDoubleList extends AbstractDoubleList {
    public final double[] items;

    public PrecomputedFractionalDoubleList(int size) {
        this.items = new double[size + 1];

        for (int i = 0; i < this.items.length; i++) {
            this.items[i] = (double) i / (double) size;
        }
    }

    @Override
    public double getDouble(int idx) {
        return this.items[idx];
    }

    @Override
    public int size() {
        return this.items.length;
    }

}
