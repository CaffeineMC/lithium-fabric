package me.jellysquid.mods.lithium.common.entity.block_tracking;

import me.jellysquid.mods.lithium.common.block.BlockStateFlags;
import me.jellysquid.mods.lithium.common.block.ListeningBlockStatePredicate;
import me.jellysquid.mods.lithium.common.util.deduplication.LithiumInternerWrapper;
import me.jellysquid.mods.lithium.common.util.tuples.WorldSectionBox;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class SectionedFluidChangeTracker extends SectionedBlockChangeTracker {
    public SectionedFluidChangeTracker(WorldSectionBox trackedWorldSections, ListeningBlockStatePredicate blockGroup) {
        super(trackedWorldSections, blockGroup);
    }

    public boolean matchesMovedBox(Box box) {
        return this.trackedWorldSections.matchesRelevantFluidBox(box);
    }

    public static SectionedFluidChangeTracker registerAt(World world, Box entityBoundingBox) {
        WorldSectionBox worldSectionBox = WorldSectionBox.relevantFluidBox(world, entityBoundingBox);
        SectionedFluidChangeTracker tracker = new SectionedFluidChangeTracker(worldSectionBox, BlockStateFlags.FLUIDS);
        //noinspection unchecked
        tracker = (SectionedFluidChangeTracker) ((LithiumInternerWrapper<SectionedBlockChangeTracker>)world).getCanonical(tracker);

        tracker.register();
        return tracker;
    }
}
