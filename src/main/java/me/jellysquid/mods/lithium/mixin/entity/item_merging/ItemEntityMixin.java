package me.jellysquid.mods.lithium.mixin.entity.item_merging;

import java.util.List;
import java.util.function.Predicate;
import java.util.Iterator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Redirect;

import me.jellysquid.mods.lithium.common.entity.item_merging.MergableItem;
import me.jellysquid.mods.lithium.common.entity.item_merging.MergableCacheInterface;
import me.jellysquid.mods.lithium.common.world.WorldHelper;

import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.function.LazyIterationConsumer;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.entity.SectionedEntityCache;
import com.google.common.collect.Lists;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin implements MergableItem {
    @Shadow
    public abstract boolean canMerge();

    @Override
    public boolean canEntityMerge() {
        return this.canMerge();
    }

    @Redirect(
        method = "tryMerge()V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;getEntitiesByClass(Ljava/lang/Class;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;)Ljava/util/List;"
        )
    )
    private <T extends Entity> List<T> redirectGetEntitiesByClass(World world, Class<T> entityClass, Box box, Predicate<? super T> predicate) {
        SectionedEntityCache<Entity> entityCache = WorldHelper.getEntityCacheOrNull(world);
        if (entityCache == null) {
            return world.getEntitiesByClass(entityClass, box, predicate);
        }

        List<T> entities = Lists.newArrayList();
        entityCache.forEachInBox(box, section -> {
            Iterator<ItemEntity> iter = ((MergableCacheInterface) section).getMergables().iterator();
            while (iter.hasNext()) {
                ItemEntity entity = iter.next();
                if (entity.getBoundingBox().intersects(box) && predicate.test((T) entity)) {
                    entities.add((T) entity);
                } else if (!((MergableItem) entity).canEntityMerge()) {
                    iter.remove();
                }
            }
            return LazyIterationConsumer.NextIteration.CONTINUE;
        });

        return entities;
    }
}
