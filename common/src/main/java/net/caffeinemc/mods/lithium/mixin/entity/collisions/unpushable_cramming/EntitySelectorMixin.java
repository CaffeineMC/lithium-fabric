package net.caffeinemc.mods.lithium.mixin.entity.collisions.unpushable_cramming;

import net.caffeinemc.mods.lithium.common.entity.pushable.EntityPushablePredicate;
import net.minecraft.world.entity.EntitySelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Predicate;

@Mixin(EntitySelector.class)
public class EntitySelectorMixin {

    @Redirect(
            method = "pushableBy(Lnet/minecraft/world/entity/Entity;)Ljava/util/function/Predicate;",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/function/Predicate;and(Ljava/util/function/Predicate;)Ljava/util/function/Predicate;"
            )
    )
    private static <T> Predicate<T> getEntityPushablePredicate(Predicate<T> first, Predicate<? super T> second) {
        return EntityPushablePredicate.and(first, second);
    }
}
