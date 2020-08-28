package me.jellysquid.mods.lithium.mixin.entity.collisions;

import me.jellysquid.mods.lithium.common.entity.LithiumEntityCollisions;
import me.jellysquid.mods.lithium.common.entity.movement.BlockCollisionPredicate;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.CollisionView;
import net.minecraft.world.EntityView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Replaces collision testing methods with jumps to our own (faster) entity collision testing code.
 */
@Mixin(CollisionView.class)
public interface CollisionViewMixin {
    /**
     * @reason Use a faster implementation
     * @author JellySquid
     */
    @Overwrite
    default Stream<VoxelShape> getBlockCollisions(final Entity entity, Box box) {
        return LithiumEntityCollisions.getBlockCollisions((CollisionView) this, entity, box, BlockCollisionPredicate.ANY);
    }

    /**
     * @reason Avoid usage of streams
     * @author JellySquid
     */
    @Overwrite
    default boolean doesNotCollide(Entity entity, Box box, Predicate<Entity> predicate) {
        boolean ret = !LithiumEntityCollisions.doesBoxCollideWithBlocks((CollisionView) this, entity, box, BlockCollisionPredicate.ANY);

        // If no blocks were collided with, try to check for entity collisions if we can read entities
        if (ret && this instanceof EntityView) {
            ret = !LithiumEntityCollisions.doesBoxCollideWithEntities((EntityView) this, entity, box, predicate);
        }

        return ret;
    }
}