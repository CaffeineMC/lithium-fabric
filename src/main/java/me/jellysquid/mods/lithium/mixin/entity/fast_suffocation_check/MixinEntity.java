package me.jellysquid.mods.lithium.mixin.entity.fast_suffocation_check;

import me.jellysquid.mods.lithium.common.entity.movement.BlockCollisionPredicate;
import me.jellysquid.mods.lithium.common.entity.movement.ChunkAwareBlockCollisionSweeper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.BiPredicate;
import java.util.stream.Stream;

@Mixin(Entity.class)
public abstract class MixinEntity {
    /**
     * @author JellySquid
     * @reason Use optimized block volume iteration, avoid streams
     */
    @Redirect(method = "isInsideWall", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockCollisions(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;Ljava/util/function/BiPredicate;)Ljava/util/stream/Stream;"))
    public Stream<VoxelShape> isInsideWall(World world, Entity entity, Box box, BiPredicate<BlockState, BlockPos> biPredicate) {
        final ChunkAwareBlockCollisionSweeper sweeper = new ChunkAwareBlockCollisionSweeper(world, (Entity) (Object) this, box,
                BlockCollisionPredicate.SUFFOCATES);
        final VoxelShape shape = sweeper.getNextCollidedShape();

        if (shape != null) {
            return Stream.of(shape);
        }

        return Stream.empty();
    }
}
