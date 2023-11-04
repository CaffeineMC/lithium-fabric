package me.jellysquid.mods.lithium.mixin.entity.item_merging;

import net.minecraft.entity.ItemEntity;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.entity.EntityTrackingSection;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.google.common.collect.Lists;

import me.jellysquid.mods.lithium.common.entity.item_merging.MergableItem;
import me.jellysquid.mods.lithium.common.entity.item_merging.MergableCacheInterface;

@Mixin(EntityTrackingSection.class)
public abstract class EntityTrackingSectionMixin<T extends EntityLike> implements MergableCacheInterface {
    private final List<ItemEntity> mergableItemEntities = Lists.newArrayList();
    private final List<ItemEntity> mergableItemEntitiesHalfEmpty = Lists.newArrayList();

    @Inject(method = "add(Lnet/minecraft/world/entity/EntityLike;)V", at = @At("RETURN"))
    private void onEntityAdded(T entityLike, CallbackInfo ci) {
        if (entityLike instanceof ItemEntity entity) {
            MergableItem mergableItem = (MergableItem) entity;
            if (mergableItem.canEntityMerge()) {
                mergableItemEntities.add(entity);

                if (mergableItem.isMoreEmpty()) {
                    mergableItemEntitiesHalfEmpty.add(entity);
                    mergableItem.setCachedState(MergableItem.CACHED_HALFEMPTY);
                } else {
                    mergableItem.setCachedState(MergableItem.CACHED_MOREFULL);
                }
            }
        }
    }

    @Inject(method = "remove(Lnet/minecraft/world/entity/EntityLike;)Z", at = @At("RETURN"))
    private void onEntityRemoved(T entityLike, CallbackInfoReturnable<Boolean> cir) {
        if (entityLike instanceof ItemEntity entity) {
            MergableItem mergableItem = (MergableItem) entity;
            byte cachedState = mergableItem.getCachedState();

            if (cachedState == MergableItem.CACHED_HALFEMPTY) {
                mergableItemEntitiesHalfEmpty.remove(entity);
                mergableItemEntities.remove(entity);
            } else if (cachedState == MergableItem.CACHED_MOREFULL) {
                mergableItemEntities.remove(entity);
            }

            mergableItem.setCachedState(MergableItem.UNCACHED);
        }
    }

    @Override
    public void forEachMergables(MergableItem item, Consumer<ItemEntity> consumer) {
        boolean canMergeItself = item.canMergeItself();
        List<ItemEntity> list = canMergeItself ? mergableItemEntities : mergableItemEntitiesHalfEmpty;
        Iterator<ItemEntity> iterator = list.iterator();

        while (iterator.hasNext()) {
            ItemEntity entity = iterator.next();
            MergableItem mergableItem = (MergableItem) entity;

            if (!mergableItem.canEntityMerge()) {
                iterator.remove();

                if (!canMergeItself) {
                    mergableItemEntities.remove(entity);
                } else if (mergableItem.getCachedState() == MergableItem.CACHED_HALFEMPTY) {
                    mergableItemEntitiesHalfEmpty.remove(entity);
                }

                mergableItem.setCachedState(MergableItem.UNCACHED);
                continue;
            }

            if (mergableItem.getCachedState() == MergableItem.CACHED_HALFEMPTY && !mergableItem.isMoreEmpty()) {
                if (canMergeItself) {
                    mergableItemEntitiesHalfEmpty.remove(entity);
                } else {
                    iterator.remove();
                    continue;
                }

                mergableItem.setCachedState(MergableItem.CACHED_MOREFULL);
            }

            if (item == mergableItem) {
                continue;
            }

            consumer.accept(entity);
        }
    }
}
