package me.jellysquid.mods.lithium.mixin.entity.stream_entity_collisions_lazily;

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
     * The resulting stream will only evaluate the getEntityCollisions lambda when it is queried.
     *
     * The intended usecase are entities standing on the ground:
     * Their gravity movement is completely blocked by the block below, querying entity collisions can't
     * affect the movement anymore
     */
    @Redirect(method = "adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getEntityCollisions(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;)Ljava/util/stream/Stream;"))
    private Stream<VoxelShape> getLazyEntityCollisionStream(World world, Entity entity_1, Box box_1, Predicate<Entity> predicate_1) {
        Stream<Supplier<Stream<VoxelShape>>> getCollisionsLazilyStream = Stream.of(() -> entity_1.world.getEntityCollisions(entity_1, box_1, predicate_1));
        //flatmap will only evaluate the supplier when the stream is reaching it. Won't be evaluated when block collisions cancel all movement before!
        return getCollisionsLazilyStream.flatMap(Supplier::get);
    }

    /**
     * Redirect to try to collide with world border first, so the entity stream doesn't have to be used when other collisions cancel the whole movement already.
     */
    @Redirect(method = "adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;",
            at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;concat(Ljava/util/stream/Stream;Ljava/util/stream/Stream;)Ljava/util/stream/Stream;"))
    private Stream<VoxelShape> reorderStreams_WorldBorderCollisionsFirst(Stream<? extends VoxelShape> entityShapes, Stream<? extends VoxelShape> blockShapes){
        return Stream.concat(blockShapes, entityShapes);
    }


    /**
     * Redirect to try to collide with blocks first, so the entity stream doesn't have to be used when block collisions cancel the whole movement already.
     */
    @Redirect(method = "adjustMovementForCollisions(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Lnet/minecraft/world/World;Lnet/minecraft/block/ShapeContext;Lnet/minecraft/util/collection/ReusableStream;)Lnet/minecraft/util/math/Vec3d;",
            at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;concat(Ljava/util/stream/Stream;Ljava/util/stream/Stream;)Ljava/util/stream/Stream;"))
    private static Stream<VoxelShape> reorderStreams_BlockCollisionsFirst(Stream<? extends VoxelShape> entityShapes, Stream<? extends VoxelShape> blockShapes){
        return Stream.concat(blockShapes, entityShapes);
    }
}
