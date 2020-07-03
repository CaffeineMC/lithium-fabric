package me.jellysquid.mods.lithium.mixin.entity.collisions;

import me.jellysquid.mods.lithium.common.entity.LithiumEntityCollisions;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.EntityView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Replaces collision testing methods with jumps to our own (faster) entity collision testing code.
 */
@Mixin(EntityView.class)
public interface EntityViewMixin {
    /**
     * @reason Avoid usage of heavy stream code
     * @author JellySquid
     */
    @Overwrite
    default Stream<VoxelShape> getEntityCollisions(Entity entity, Box box, Predicate<Entity> predicate) {
        return LithiumEntityCollisions.getEntityCollisions((EntityView) this, entity, box, predicate);
    }
}
