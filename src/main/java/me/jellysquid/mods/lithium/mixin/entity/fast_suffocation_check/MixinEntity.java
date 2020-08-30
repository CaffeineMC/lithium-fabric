package me.jellysquid.mods.lithium.mixin.entity.fast_suffocation_check;

import me.jellysquid.mods.lithium.common.entity.LithiumEntityCollisions;
import me.jellysquid.mods.lithium.common.entity.movement.BlockCollisionPredicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Shadow
    private EntityDimensions dimensions;

    @Shadow
    public boolean noClip;

    @Shadow
    public World world;

    @Shadow
    public abstract double getX();

    @Shadow
    public abstract double getEyeY();

    @Shadow
    public abstract double getZ();

    /**
     * @author JellySquid
     * @reason Use optimized block volume iteration, avoid streams
     */
    @Overwrite
    public boolean isInsideWall() {
        if (this.noClip) {
            return false;
        }

        float width = this.dimensions.width * 0.8F;

        Box box = Box.method_30048(width, 0.1D, width)
                .offset(this.getX(), this.getEyeY(), this.getZ());

        return LithiumEntityCollisions.doesBoxCollideWithBlocks(this.world, (Entity) (Object) this, box,
                BlockCollisionPredicate.SUFFOCATES);
    }
}
