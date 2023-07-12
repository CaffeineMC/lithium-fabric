package me.jellysquid.mods.lithium.common.entity.block_tracking;

import it.unimi.dsi.fastutil.objects.Reference2LongArrayMap;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public final class FluidListeningInfo {
    private static final int MIN_STATIONARY_COUNT = 16;
    private Box cachedPos;
    private SectionedFluidChangeTracker tracker;
    private Reference2LongArrayMap<TagKey<Fluid>> lastNotTouchedFluidTimes;
    private int stationary; //Stationary field can be left out, but is intended to avoid spamming the block tracking system with entities that are currently moving.

    public FluidListeningInfo() {
        this.tracker = null;
        this.lastNotTouchedFluidTimes = null;
        this.cachedPos = null;
        this.stationary = 0;
    }

    public void updateTracker(Box boundingBox, World world) {
        if (!boundingBox.equals(this.cachedPos)) {
            if (this.tracker != null) {
                if (!this.tracker.matchesMovedBox(boundingBox)) {
                    this.tracker.unregister();
                    this.tracker = null;
                }
            }
            this.cachedPos = boundingBox;
            this.stationary = 0;
            if (this.lastNotTouchedFluidTimes != null) {
                this.lastNotTouchedFluidTimes.clear();
            }
            return;
        }
        if (this.stationary >= MIN_STATIONARY_COUNT) {
            if (this.tracker == null && this.lastNotTouchedFluidTimes != null && !this.lastNotTouchedFluidTimes.isEmpty()) {
                this.tracker = SectionedFluidChangeTracker.registerAt(world, boundingBox);
            }
        } else {
            this.stationary++;
        }
    }

    public boolean cachedIsNotTouchingFluid(TagKey<Fluid> tag) {
        if (this.stationary >= MIN_STATIONARY_COUNT && this.tracker != null && this.lastNotTouchedFluidTimes != null) {
            long cachedTime = this.lastNotTouchedFluidTimes.getOrDefault(tag, Long.MIN_VALUE);
            return this.tracker.isUnchangedSince(cachedTime);
        }
        return false;
    }

    public void cacheNotTouchingFluid(TagKey<Fluid> tag, long time) {
        if (this.lastNotTouchedFluidTimes == null) {
            this.lastNotTouchedFluidTimes = new Reference2LongArrayMap<>();
        }
        this.lastNotTouchedFluidTimes.put(tag, time);
    }

    public void remove() {
        if (this.tracker != null) {
            this.tracker.unregister();
        }
    }
}
