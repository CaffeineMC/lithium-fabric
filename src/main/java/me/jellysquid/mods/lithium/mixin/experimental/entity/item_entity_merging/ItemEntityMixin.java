package me.jellysquid.mods.lithium.mixin.experimental.entity.item_entity_merging;

import me.jellysquid.mods.lithium.common.entity.TypeFilterableListInternalAccess;
import me.jellysquid.mods.lithium.common.entity.item.ItemEntityLazyIterationConsumer;
import me.jellysquid.mods.lithium.common.entity.item.ItemEntityList;
import me.jellysquid.mods.lithium.common.world.WorldHelper;
import me.jellysquid.mods.lithium.mixin.util.accessors.EntityTrackingSectionAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.TypeFilterableList;
import net.minecraft.util.function.LazyIterationConsumer;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.entity.SectionedEntityCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {

    public ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    public abstract ItemStack getStack();

    @Redirect(
            method = "tryMerge()V",
            at = @At(
                    value = "INVOKE", target = "Lnet/minecraft/world/World;getEntitiesByClass(Ljava/lang/Class;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;)Ljava/util/List;"
            )
    )
    private List<ItemEntity> getItems(World world, Class<ItemEntity> itemEntityClass, Box box, Predicate<ItemEntity> predicate) {
        SectionedEntityCache<Entity> cache = WorldHelper.getEntityCacheOrNull(world);
        if (cache != null) {
            return consumeItemEntitiesForMerge(cache, (ItemEntity) (Object) this, box, predicate);
        }

        return world.getEntitiesByClass(itemEntityClass, box, predicate);
    }


    @Unique
    private static ArrayList<ItemEntity> consumeItemEntitiesForMerge(SectionedEntityCache<Entity> cache, ItemEntity searchingItemEntity, Box box, Predicate<ItemEntity> predicate) {
        ItemEntityLazyIterationConsumer itemEntityConsumer = new ItemEntityLazyIterationConsumer(searchingItemEntity, box, predicate);
        cache.forEachInBox(box, section -> {
            //noinspection unchecked
            TypeFilterableList<Entity> allEntities = ((EntityTrackingSectionAccessor<Entity>) section).getCollection();

            //noinspection unchecked
            TypeFilterableListInternalAccess<Entity> internalEntityList = (TypeFilterableListInternalAccess<Entity>) allEntities;
            List<ItemEntity> itemEntities = internalEntityList.lithium$getOrCreateAllOfTypeRaw(ItemEntity.class);


            LazyIterationConsumer.NextIteration next = LazyIterationConsumer.NextIteration.CONTINUE;
            if (itemEntities instanceof ItemEntityList itemEntityList) {
                next = itemEntityList.consumeForEntityStacking(searchingItemEntity, itemEntityConsumer);
            } else if (itemEntities.size() > ItemEntityList.UPGRADE_THRESHOLD && itemEntities instanceof ArrayList<ItemEntity>) {
                ItemEntityList itemEntityList = (ItemEntityList) internalEntityList.lithium$replaceCollectionAndGet(ItemEntity.class, ItemEntityList::new);
                next = itemEntityList.consumeForEntityStacking(searchingItemEntity, itemEntityConsumer);
            } else {
                for (int i = 0; next != LazyIterationConsumer.NextIteration.ABORT && i < itemEntities.size(); i++) {
                    ItemEntity entity = itemEntities.get(i);
                    next = itemEntityConsumer.accept(entity);
                }
            }
            return next;
        });
        return itemEntityConsumer.getMergeEntities();
    }


}
