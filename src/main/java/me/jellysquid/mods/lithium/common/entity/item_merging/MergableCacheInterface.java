package me.jellysquid.mods.lithium.common.entity.item_merging;

import java.util.List;

import net.minecraft.entity.ItemEntity;

public interface MergableCacheInterface {
    void updateMergable(ItemEntity entity);
    List<ItemEntity> getMergables();
}
