package me.jellysquid.mods.lithium.common.entity.item_merging;

import java.util.function.Consumer;
import net.minecraft.entity.ItemEntity;

public interface MergableCacheInterface {
    void forEachMergables(MergableItem item, Consumer<ItemEntity> consumer);
}
