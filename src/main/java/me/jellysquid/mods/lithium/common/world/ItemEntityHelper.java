package me.jellysquid.mods.lithium.common.world;

import me.jellysquid.mods.lithium.common.entity.TypeFilterableListInternalAccess;
import me.jellysquid.mods.lithium.common.entity.item.ItemEntityCategorizingList;
import me.jellysquid.mods.lithium.mixin.util.accessors.EntityTrackingSectionAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.collection.TypeFilterableList;
import net.minecraft.util.function.LazyIterationConsumer;
import net.minecraft.util.math.Box;
import net.minecraft.world.entity.SectionedEntityCache;

import java.util.ArrayList;
import java.util.List;

public class ItemEntityHelper {

    public static void consumeItemEntitiesForMerge(SectionedEntityCache<Entity> cache, ItemEntity searchingItemEntity, Box box, LazyIterationConsumer<ItemEntity> itemEntityConsumer) {
        cache.forEachInBox(box, section -> {
            //noinspection unchecked
            TypeFilterableList<Entity> allEntities = ((EntityTrackingSectionAccessor<Entity>) section).getCollection();

            //noinspection unchecked
            TypeFilterableListInternalAccess<Entity> internalEntityList = (TypeFilterableListInternalAccess<Entity>) allEntities;
            List<ItemEntity> itemEntities = internalEntityList.lithium$getOrCreateAllOfTypeRaw(ItemEntity.class);
            if (itemEntities.size() > ItemEntityCategorizingList.UPGRADE_THRESHOLD && itemEntities instanceof ArrayList<ItemEntity>) {
                itemEntities = internalEntityList.lithium$replaceCollectionAndGet(ItemEntity.class, ItemEntityCategorizingList::wrapDelegate);
            }

            LazyIterationConsumer.NextIteration next = LazyIterationConsumer.NextIteration.CONTINUE;
            if (itemEntities instanceof ItemEntityCategorizingList categorizingList) {
                next = categorizingList.consumeForEntityStacking(searchingItemEntity, itemEntityConsumer);
            } else {
                for (int i = 0; next != LazyIterationConsumer.NextIteration.ABORT && i < itemEntities.size(); i++) {
                    ItemEntity entity = itemEntities.get(i);
                    next = itemEntityConsumer.accept(entity);
                }
            }
            return next;
        });
    }
}
