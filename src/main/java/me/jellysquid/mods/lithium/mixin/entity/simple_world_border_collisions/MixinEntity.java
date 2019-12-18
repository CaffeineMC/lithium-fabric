package me.jellysquid.mods.lithium.mixin.entity.simple_world_border_collisions;

import me.jellysquid.mods.lithium.common.LithiumMod;
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
    @Redirect(method = "adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/shape/VoxelShapes;matchesAnywhere(Lnet/minecraft/util/shape/VoxelShape;Lnet/minecraft/util/shape/VoxelShape;Lnet/minecraft/util/BooleanBiFunction;)Z"))
    private boolean redirectWorldBorderMatchesAnywhere(VoxelShape borderShape, VoxelShape entityShape, BooleanBiFunction func, Vec3d motion) {
        if (!LithiumMod.CONFIG.physics.useFastWorldBorderChecks) {
            return VoxelShapes.matchesAnywhere(borderShape, entityShape, func);
        }

        WorldBorder border = this.world.getWorldBorder();

        double wboxMinX = border.getCenterX() - (border.getSize() / 2);
        double wboxMinZ = border.getCenterZ() - (border.getSize() / 2);

        double wboxMaxX = border.getCenterX() + (border.getSize() / 2);
        double wboxMaxZ = border.getCenterZ() + (border.getSize() / 2);

        Box ebox = this.getBoundingBox().stretch(motion);

        // The entity's bounding box is fully within the world border and will not collide with it. We can safely remove
        // it from the stream of shapes to do collision checking against.
        if (wboxMinX < ebox.x1 && wboxMaxX > ebox.x1 && wboxMinX < ebox.x2 && wboxMaxX > ebox.x2 &&
                wboxMinZ < ebox.z1 && wboxMaxZ > ebox.z1 && wboxMinZ < ebox.z2 && wboxMaxZ > ebox.z2) {
            return true;
        }

        return VoxelShapes.matchesAnywhere(borderShape, entityShape, func);
    }


}
