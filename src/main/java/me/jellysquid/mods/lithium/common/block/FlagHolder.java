package me.jellysquid.mods.lithium.common.block;

public interface FlagHolder {
    boolean getFlag(Flag.CachedFlag cachedFlag);

    int getAllFlags();
}
