package me.jellysquid.mods.lithium.mixin.entity.simple_world_border_collisions;

import me.jellysquid.mods.lithium.common.LithiumMod;
import me.jellysquid.mods.lithium.common.shapes.LithiumEntityCollisions;
import net.minecraft.entity.Entity;
import net.minecraft.util.BooleanBiFunction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Shadow
    public World world;

    @Shadow
    public abstract Box getBoundingBox();

    /**
     * Uses a very quick check to determine if the world border should even be considered in collision resolution.
     */
    @Redirect(method = "handleCollisions", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/shape/VoxelShapes;matchesAnywhere(Lnet/minecraft/util/shape/VoxelShape;Lnet/minecraft/util/shape/VoxelShape;Lnet/minecraft/util/BooleanBiFunction;)Z"))
    private boolean redirectWorldBorderMatchesAnywhere(VoxelShape borderShape, VoxelShape entityShape, BooleanBiFunction func, Vec3d motion) {
        if (!LithiumMod.CONFIG.physics.useFastWorldBorderChecks) {
            return VoxelShapes.matchesAnywhere(borderShape, entityShape, func);
        }

        WorldBorder border = this.world.getWorldBorder();
        Box ebox = this.getBoundingBox().stretch(motion);
        return LithiumEntityCollisions.doesEntityCollideWithWorldBorder(border, ebox);
    }
}
