package me.jellysquid.mods.lithium.mixin.entity.collisions.intersection;

import me.jellysquid.mods.lithium.common.world.WorldHelper;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.world.EntityView;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.function.Predicate;

@Mixin(EntityView.class)
public interface EntityViewMixin {

    @Redirect(
            method = "getEntityCollisions(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;)Ljava/util/List;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/EntityView;getOtherEntities(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;)Ljava/util/List;")
    )
    private List<Entity> getCollisionEntities(EntityView instance, @Nullable Entity entity, Box box, Predicate<? super Entity> predicate) {
        return WorldHelper.getOtherEntitiesForCollision(instance, box, entity, predicate);
    }
}
