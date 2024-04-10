package me.jellysquid.mods.lithium.common.world.chunk;

import me.jellysquid.mods.lithium.common.entity.EntityClassGroup;

import java.util.Collection;

public interface ClassGroupFilterableList<T> {
    Collection<T> lithium$getAllOfGroupType(EntityClassGroup type);

}