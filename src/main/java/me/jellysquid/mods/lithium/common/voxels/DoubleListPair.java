package me.jellysquid.mods.lithium.common.voxels;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.util.shape.IdentityListMerger;

/**
 * Fake version of {@link IdentityListMerger} designed for implementing {@link net.minecraft.util.shape.DoubleListPair}
 */
public abstract class DoubleListPair extends IdentityListMerger {
    public DoubleListPair() {
        super(null);
    }

    @Override
    public abstract DoubleList getMergedList();

    @Override
    public abstract boolean forAllOverlappingSections(SectionPairPredicate predicate);
}