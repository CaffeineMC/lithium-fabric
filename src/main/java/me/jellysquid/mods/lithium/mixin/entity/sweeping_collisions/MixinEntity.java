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
    @Redirect(method = "adjustMovementForCollisions(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Lnet/minecraft/world/World;Lnet/minecraft/entity/EntityContext;Lnet/minecraft/util/ReusableStream;)Lnet/minecraft/util/math/Vec3d;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockCollisions(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;)Ljava/util/stream/Stream;", ordinal = 0))
    private static Stream<VoxelShape> adjustMovementForCollisions(World unusedWorld, Entity unusedEntity, Box unusedBox, Entity entity, Vec3d movement, Box entityBoundingBox, World world, EntityContext context, ReusableStream<VoxelShape> collisions) {
        return LithiumEntityCollisions.getBlockCollisionsSweeping(world, entity, entityBoundingBox, movement);
    }
}
