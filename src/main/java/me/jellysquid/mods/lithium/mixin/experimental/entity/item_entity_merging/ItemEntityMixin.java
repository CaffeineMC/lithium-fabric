package me.jellysquid.mods.lithium.mixin.experimental.entity.item_entity_merging;

import me.jellysquid.mods.lithium.common.entity.TypeFilterableListInternalAccess;
import me.jellysquid.mods.lithium.common.entity.item.ItemEntityCategorizingList;
import me.jellysquid.mods.lithium.common.entity.item.ItemEntityLazyIterationConsumer;
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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.Iterator;
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
            ItemEntityLazyIterationConsumer itemEntityConsumer = new ItemEntityLazyIterationConsumer((ItemEntity) (Object) this, box, predicate);
            consumeItemEntitiesForMerge(cache, (ItemEntity) (Object) this, box, itemEntityConsumer);
            return itemEntityConsumer.getMergeEntities();
        }

        return world.getEntitiesByClass(itemEntityClass, box, predicate);
    }


    @Redirect(
        method = "merge(Lnet/minecraft/entity/ItemEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/ItemEntity;Lnet/minecraft/item/ItemStack;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;discard()V")
    )
    private static void delayEntityRemoval(ItemEntity itemEntity) {
        //We have to skip discarding the item entity, so the backing collection of the iterator is not modified here.
        // Instead, we first remove the element from the iterator before discarding it. See the iterator definition
        // below. We cannot invoke iterator.remove() here, because we don't have access to the iterator variable here.

        //TODO only do this when the caller is the vanilla code path (other mods could call this in other places)
        // otherwise negative side effect: entity removal is delayed until it ticks, damaging it is possible, additional game event is detectable
    }
    @Inject(
            method = "tryMerge()V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;isRemoved()Z", shift = At.Shift.BEFORE),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void delayedEntityRemoval(CallbackInfo ci, List<ItemEntity> items, Iterator<ItemEntity> iterator, ItemEntity itemEntity) {
        //We previously set the health to be negative instead of removing the item entity. Now we can handle the
        // removal, including avoiding ConcurrentModificationException in the iterator.
        if (itemEntity.getStack().isEmpty()) {
            //The iterator is used further, we have to call iterator.remove() here
            iterator.remove();
            itemEntity.discard();
        }
        if (this.getStack().isEmpty()) {
            //The iterator is discarded immediately, so no need to call iterator.remove()
            this.discard();
        }
    }

    @Unique
    private static void consumeItemEntitiesForMerge(SectionedEntityCache<Entity> cache, ItemEntity searchingItemEntity, Box box, LazyIterationConsumer<ItemEntity> itemEntityConsumer) {
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
                searchingItemEntity.
                        next = categorizingList.consumeForEntityStacking(searchingItemEntity, itemEntityConsumer);
            } else {
                for (int i = 0; next != LazyIterationConsumer.NextIteration.ABORT && i < itemEntities.size(); i++) {
                    ItemEntity entity = itemEntities.get(i);
                    searchingItemEntity.accept(entity);
                    next = itemEntityConsumer.accept(entity);
                }
            }
            return next;
        });
    }


}
