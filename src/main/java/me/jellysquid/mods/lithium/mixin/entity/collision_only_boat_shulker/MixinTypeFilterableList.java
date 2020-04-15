package me.jellysquid.mods.lithium.mixin.entity.collision_only_boat_shulker;

import me.jellysquid.mods.lithium.common.entity.LithiumEntityCollisions;
import net.minecraft.util.collection.TypeFilterableList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Patches {@link TypeFilterableList} to allow grouping entities.
 * @author 2No2Name
 */
@Mixin(value = TypeFilterableList.class, priority = 1001)
public class MixinTypeFilterableList<T> {

    @Redirect(require = 0, method = "getAllOfType", at = @At(value = "INVOKE", target = "Ljava/lang/Class;isAssignableFrom(Ljava/lang/Class;)Z"))
    private boolean isAssignableFrom(Class<?> aClass, Class<?> bClass) {
        return bClass == LithiumEntityCollisions.CollisionBoxOverridingEntity.class || aClass.isAssignableFrom(bClass);
    }
}
