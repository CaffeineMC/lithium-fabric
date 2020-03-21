package me.jellysquid.mods.lithium.mixin.entity.simple_entity_block_collisions;

import me.jellysquid.mods.lithium.common.entity.LithiumEntityCollisions;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.CollisionView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.stream.Stream;

/**
 * Replaces collision testing methods with jumps to our own (faster) entity collision testing code.
 */
@Mixin(CollisionView.class)
public interface MixinCollisionView {
    /**
     * @reason Use a faster implementation
     * @author JellySquid
     */
    @Overwrite
    default Stream<VoxelShape> getBlockCollisions(final Entity entity, Box box) {
        return LithiumEntityCollisions.getBlockCollisions((CollisionView) this, entity, box);
    }
}