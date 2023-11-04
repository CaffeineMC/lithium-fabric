package me.jellysquid.mods.lithium.mixin.entity.item_merging;

import net.minecraft.entity.ItemEntity;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.entity.EntityTrackingSection;

import java.util.List;

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

    @Inject(method = "add(Lnet/minecraft/world/entity/EntityLike;)V", at = @At("RETURN"))
    private void onEntityAdded(T entityLike, CallbackInfo ci) {
        if (entityLike instanceof ItemEntity entity) {
            if (((MergableItem) entity).canEntityMerge()) {
                mergableItemEntities.add(entity);
            }
        }
    }

    @Inject(method = "remove(Lnet/minecraft/world/entity/EntityLike;)Z", at = @At("RETURN"))
    private void onEntityRemoved(T entityLike, CallbackInfoReturnable<Boolean> cir) {
        if (entityLike instanceof ItemEntity entity) {
            mergableItemEntities.remove(entity);
        }
    }

    @Override
    public List<ItemEntity> getMergables() {
        return mergableItemEntities;
    }

    @Override
    public void updateMergable(ItemEntity entity) {
        boolean mergable = ((MergableItem) entity).canEntityMerge();
        if (!mergable) {
            mergableItemEntities.remove(entity);
        }
    }
}
