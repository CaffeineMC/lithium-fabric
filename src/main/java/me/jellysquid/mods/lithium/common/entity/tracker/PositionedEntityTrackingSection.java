package me.jellysquid.mods.lithium.common.entity.tracker;

public interface PositionedEntityTrackingSection {
    void setPos(long chunkSectionPos);

    long getPos();
}
