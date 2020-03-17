package me.jellysquid.mods.lithium.mixin.avoid_allocations;

import me.jellysquid.mods.lithium.common.world.ExtendedWorld;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(ServerWorld.class)
public abstract class MixinServerWorld {
    private final BlockPos.Mutable randomPosInChunkCachedPos = new BlockPos.Mutable();

    /**
     * @reason Avoid allocating BlockPos every invocation through using our allocation-free variant
     */
    @Redirect(method = "tickChunk(Lnet/minecraft/world/chunk/WorldChunk;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getRandomPosInChunk(IIII)Lnet/minecraft/util/math/BlockPos;"))
    private BlockPos redirectTickGetRandomPosInChunk(ServerWorld serverWorld, int x, int y, int z, int mask) {
        ((ExtendedWorld) serverWorld).getRandomPosInChunk(x, y, z, mask, this.randomPosInChunkCachedPos);

        return this.randomPosInChunkCachedPos;
    }

    /**
     * @reason Ensure an immutable block position is passed on block tick
     */
    @Redirect(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;randomTick(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)V"))
    private void redirectBlockStateTick(BlockState blockState, ServerWorld world, BlockPos pos, Random rand) {
        blockState.randomTick(world, pos.toImmutable(), rand);
    }

    /**
     * @reason Ensure an immutable block position is passed on fluid tick
     */
    @Redirect(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;onRandomTick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)V"))
    private void redirectFluidStateTick(FluidState fluidState, World world, BlockPos pos, Random rand) {
        fluidState.onRandomTick(world, pos.toImmutable(), rand);
    }
}
