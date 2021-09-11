package me.jellysquid.mods.lithium.common.entity.tracker.nearby;

public interface MovementTrackerCache {
    void remove(SectionedEntityMovementTracker<?, ?> tracker);

    <S extends SectionedEntityMovementTracker<?, ?>> S deduplicate(S tracker);
}
