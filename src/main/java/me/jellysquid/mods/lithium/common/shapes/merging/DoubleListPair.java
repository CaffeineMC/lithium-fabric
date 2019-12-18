package me.jellysquid.mods.lithium.common.shapes.merging;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.util.shape.IdentityPairList;

/**
 * Fake version of {@link IdentityPairList} designed for implementing {@link net.minecraft.util.shape.PairList}
 */
public abstract class DoubleListPair extends IdentityPairList {
    public DoubleListPair() {
        super(null);
    }

    @Override
    public abstract DoubleList getPairs();

    public abstract boolean forEachPair(DoubleListPair.Consumer predicate);
}