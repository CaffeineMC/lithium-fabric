package net.caffeinemc.mods.lithium.mixin.experimental.entity.item_entity_merging;

import net.caffeinemc.mods.lithium.common.entity.TypeFilterableListInternalAccess;
import net.caffeinemc.mods.lithium.common.entity.item.ItemEntityLazyIterationConsumer;
import net.caffeinemc.mods.lithium.common.entity.item.ItemEntityList;
import net.caffeinemc.mods.lithium.common.world.WorldHelper;
import net.caffeinemc.mods.lithium.mixin.util.accessors.EntitySectionAccessor;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntitySectionStorage;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {

    public ItemEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Redirect(
            method = "mergeWithNeighbours()V",
            at = @At(
                    value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;"
            )
    )
    private List<ItemEntity> getItems(Level world, Class<ItemEntity> itemEntityClass, AABB box, Predicate<ItemEntity> predicate) {
        EntitySectionStorage<Entity> cache = WorldHelper.getEntityCacheOrNull(world);
        if (cache != null) {
            return consumeItemEntitiesForMerge(cache, (ItemEntity) (Object) this, box, predicate);
        }

        return world.getEntitiesOfClass(itemEntityClass, box, predicate);
    }


    @Unique
    private static ArrayList<ItemEntity> consumeItemEntitiesForMerge(EntitySectionStorage<Entity> cache, ItemEntity searchingItemEntity, AABB box, Predicate<ItemEntity> predicate) {
        ItemEntityLazyIterationConsumer itemEntityConsumer = new ItemEntityLazyIterationConsumer(searchingItemEntity, box, predicate);
        cache.forEachAccessibleNonEmptySection(box, section -> {
            //noinspection unchecked
            ClassInstanceMultiMap<Entity> allEntities = ((EntitySectionAccessor<Entity>) section).getCollection();

            //noinspection unchecked
            TypeFilterableListInternalAccess<Entity> internalEntityList = (TypeFilterableListInternalAccess<Entity>) allEntities;
            List<ItemEntity> itemEntities = internalEntityList.lithium$getOrCreateAllOfTypeRaw(ItemEntity.class);


            AbortableIterationConsumer.Continuation next = AbortableIterationConsumer.Continuation.CONTINUE;
            if (itemEntities instanceof ItemEntityList itemEntityList) {
                next = itemEntityList.consumeForEntityStacking(searchingItemEntity, itemEntityConsumer);
            } else if (itemEntities.size() > ItemEntityList.UPGRADE_THRESHOLD && itemEntities instanceof ArrayList<ItemEntity>) {
                ItemEntityList itemEntityList = (ItemEntityList) internalEntityList.lithium$replaceCollectionAndGet(ItemEntity.class, ItemEntityList::new);
                next = itemEntityList.consumeForEntityStacking(searchingItemEntity, itemEntityConsumer);
            } else {
                for (int i = 0; next != AbortableIterationConsumer.Continuation.ABORT && i < itemEntities.size(); i++) {
                    ItemEntity entity = itemEntities.get(i);
                    next = itemEntityConsumer.accept(entity);
                }
            }
            return next;
        });
        return itemEntityConsumer.getMergeEntities();
    }


}
