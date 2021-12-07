package me.jellysquid.mods.lithium.mixin.entity.collisions.intersection;

import me.jellysquid.mods.lithium.common.entity.LithiumEntityCollisions;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.world.CollisionView;
import net.minecraft.world.EntityView;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * Replaces collision testing methods with jumps to our own (faster) entity collision testing code.
 */
@Mixin(CollisionView.class)
public interface CollisionViewMixin {

    /**
     * Checks whether the area is empty from blocks, hard entities and the world border.
     *
     * @reason Only access relevant entity classes, use more efficient block access
     * @author 2No2Name
     */
    @Overwrite
    default boolean isSpaceEmpty(@Nullable Entity entity, Box box) {
        boolean ret = !LithiumEntityCollisions.doesBoxCollideWithBlocks((CollisionView) this, entity, box);

        // If no blocks were collided with, try to check for entity collisions if we can read entities
        if (ret && this instanceof EntityView) {
            //needs to include world border collision
            ret = !LithiumEntityCollisions.doesBoxCollideWithHardEntities((EntityView) this, entity, box);
        }

        if (ret && entity != null) {
            ret = !LithiumEntityCollisions.doesEntityCollideWithWorldBorder((CollisionView) this, entity);
        }

        return ret;
    }
}