package me.jellysquid.mods.lithium.common.block;

public interface SectionModCounter {
    boolean isUnchanged(long modCount);

    long getModCount();
}
