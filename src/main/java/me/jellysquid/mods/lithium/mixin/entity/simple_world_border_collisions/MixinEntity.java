package me.jellysquid.mods.lithium.mixin.entity.simple_world_border_collisions;

import me.jellysquid.mods.lithium.common.entity.LithiumEntityCollisions;
import net.minecraft.entity.Entity;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Replaces collision testing methods against the world border with faster checks.
 */
@Mixin(Entity.class)
public abstract class MixinEntity {
    @Shadow
    public World world;

    @Shadow
    public abstract Box getBoundingBox();

    /**
     * Uses a very quick check to determine if the player is outside the world border (which would disable collisions
     * against it). We also perform an additional check to see if the player can even collide with the world border in
     * this physics step, allowing us to remove it from collision testing in later code.
     *
     * @return True if no collision resolution will be performed against the world border, which removes it from the
     * stream of shapes to consider in entity collision code.
     */
    @Redirect(method = "adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/shape/VoxelShapes;matchesAnywhere(Lnet/minecraft/util/shape/VoxelShape;Lnet/minecraft/util/shape/VoxelShape;Lnet/minecraft/util/function/BooleanBiFunction;)Z"))
    private boolean redirectWorldBorderMatchesAnywhere(VoxelShape borderShape, VoxelShape entityShape, BooleanBiFunction func, Vec3d motion) {
        boolean isWithinWorldBorder = LithiumEntityCollisions.isBoxFullyWithinWorldBorder(this.world.getWorldBorder(), this.getBoundingBox().contract(1.0E-7D));

        // If the entity is within the world border (enabling collisions against it), check that the player will cross the
        // border this physics step.
        if (isWithinWorldBorder) {
            return LithiumEntityCollisions.isBoxFullyWithinWorldBorder(this.world.getWorldBorder(), this.getBoundingBox().stretch(motion));
        }

        return true;
    }
}
