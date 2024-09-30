package me.jellysquid.mods.lithium.mixin.entity.collisions.intersection;

import me.jellysquid.mods.lithium.common.world.WorldHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.function.Predicate;

@Mixin(EntityGetter.class)
public interface EntityViewMixin {

    @Redirect(
            method = "getEntityCollisions(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/EntityGetter;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;")
    )
    private List<Entity> getCollisionEntities(EntityGetter instance, @Nullable Entity entity, AABB box, Predicate<? super Entity> predicate) {
        return WorldHelper.getOtherEntitiesForCollision(instance, box, entity, predicate);
    }
}
