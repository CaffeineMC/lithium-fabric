package me.jellysquid.mods.lithium.mixin.entity.sweeping_collisions;

import me.jellysquid.mods.lithium.common.shapes.LithiumEntityCollisions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityContext;
import net.minecraft.util.ReusableStream;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.stream.Stream;

@Mixin(Entity.class)
public abstract class MixinEntity {
    /**
     * @author JellySquid
     */
    @Redirect(method = "adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;",
            at = @At(value = "INVOKE",target = "Lnet/minecraft/entity/Entity;adjustMovementForCollisions(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Lnet/minecraft/world/World;Lnet/minecraft/entity/EntityContext;Lnet/minecraft/util/ReusableStream;)Lnet/minecraft/util/math/Vec3d;", ordinal = 0))
    public Vec3d adjustMovementForCollisions(Entity entity, Vec3d movement, Box entityBoundingBox, World world, EntityContext context, ReusableStream<VoxelShape> collisions) {
        boolean mx = movement.x == 0.0D;
        boolean my = movement.y == 0.0D;
        boolean mz = movement.z == 0.0D;

        if ((!mx || !my) && (!mx || !mz) && (!my || !mz)) {
            ReusableStream<VoxelShape> reusableStream = new ReusableStream<>(Stream.concat(collisions.stream(),
                    LithiumEntityCollisions.getBlockCollisionsSweeping(world, entity, entityBoundingBox, movement)));

            return Entity.adjustMovementForCollisions(movement, entityBoundingBox, reusableStream);
        }

        return Entity.adjustSingleAxisMovementForCollisions(movement, entityBoundingBox, world, context, collisions);
    }


}
