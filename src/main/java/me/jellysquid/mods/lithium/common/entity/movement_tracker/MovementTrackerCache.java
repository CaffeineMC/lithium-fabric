package me.jellysquid.mods.lithium.common.entity.movement_tracker;

public interface MovementTrackerCache {
    void lithium$remove(SectionedEntityMovementTracker<?, ?> tracker);

    <S extends SectionedEntityMovementTracker<?, ?>> S lithium$deduplicate(S tracker);
}
