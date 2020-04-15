package me.jellysquid.mods.lithium.mixin.entity.shortcut_entity_entity_collisions;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Mixin(Entity.class)
public class MixinEntity {
    /**
     * Redirect to allow skipping getting entities from world when the stream isn't used.
     * The resulting stream will only evaluate the getEntityCollisions lambda when it is queried
     */
    @Redirect(method = "adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getEntityCollisions(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;)Ljava/util/stream/Stream;"))
    private Stream<VoxelShape> getLazyCreatedEntityCollisionStream(World world, Entity entity_1, Box box_1, Predicate<Entity> predicate_1) {
        Stream<Supplier<Stream<VoxelShape>>> getCollisionsLazilyStream = Stream.of(() -> world.getEntityCollisions(entity_1, box_1, predicate_1));
        return getCollisionsLazilyStream.flatMap(Supplier::get);
    }

    /**
     * Redirect to try to collide with blocks first, so the entity stream doesn't have to be used when block collisions cancel the whole movement already.
     */
    @Redirect(method = "adjustMovementForCollisions(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Lnet/minecraft/world/World;Lnet/minecraft/block/ShapeContext;Lnet/minecraft/util/collection/ReusableStream;)Lnet/minecraft/util/math/Vec3d;",
            at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;concat(Ljava/util/stream/Stream;Ljava/util/stream/Stream;)Ljava/util/stream/Stream;"))
    private static Stream<VoxelShape> concatBlockCollisionsFirst(Stream<? extends VoxelShape> stream1, Stream<? extends VoxelShape> stream2){
        return Stream.concat(stream2, stream1);
    }
}
