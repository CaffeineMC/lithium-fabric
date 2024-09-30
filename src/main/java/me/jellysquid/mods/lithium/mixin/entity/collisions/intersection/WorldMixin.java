package me.jellysquid.mods.lithium.mixin.entity.collisions.intersection;

import me.jellysquid.mods.lithium.common.entity.LithiumEntityCollisions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Replaces collision testing methods with jumps to our own (faster) entity collision testing code.
 */
@Mixin(Level.class)
public abstract class WorldMixin implements LevelAccessor {

    /**
     * Checks whether the area is empty from blocks, hard entities and the world border.
     * Only access relevant entity classes, use more efficient block access
     * @author 2No2Name
     */
    @Override
    public boolean noCollision(@Nullable Entity entity, AABB box) {
        boolean ret = !LithiumEntityCollisions.doesBoxCollideWithBlocks((Level) (Object) this, entity, box);

        // If no blocks were collided with, try to check for entity collisions if we can read entities
        if (ret) {
            //needs to include world border collision
            ret = !LithiumEntityCollisions.doesBoxCollideWithHardEntities(this, entity, box);
        }

        if (ret && entity != null) {
            ret = !LithiumEntityCollisions.doesBoxCollideWithWorldBorder(this, entity, box);
        }

        return ret;
    }
}