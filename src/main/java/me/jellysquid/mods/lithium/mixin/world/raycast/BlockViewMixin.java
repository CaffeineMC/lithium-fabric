package me.jellysquid.mods.lithium.mixin.world.raycast;

import com.llamalad7.mixinextras.sugar.Local;
import me.jellysquid.mods.lithium.common.util.Pos;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.BiFunction;

@Mixin(BlockView.class)
public interface BlockViewMixin {

    @Shadow
    BlockState getBlockState(BlockPos pos);

    @Shadow
    @Nullable BlockHitResult raycastBlock(Vec3d start, Vec3d end, BlockPos pos, VoxelShape shape, BlockState state);

    @ModifyArg(
            method = "raycast(Lnet/minecraft/world/RaycastContext;)Lnet/minecraft/util/hit/BlockHitResult;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/BlockView;raycast(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Ljava/lang/Object;Ljava/util/function/BiFunction;Ljava/util/function/Function;)Ljava/lang/Object;"
            ),
            index = 3
    )
    private BiFunction<RaycastContext, BlockPos, BlockHitResult> blockHitFactory(BiFunction<RaycastContext, BlockPos, BlockHitResult> original, @Local(argsOnly = true) RaycastContext context) {
        if (!(this instanceof WorldView world)) {
            return original;
        }
        return new BiFunction<>() {
            int chunkX = Integer.MIN_VALUE, chunkZ = Integer.MIN_VALUE;
            Chunk chunk = null;
            final boolean handleFluids = ((RaycastContextAccessor) context).getFluidHandling() != RaycastContext.FluidHandling.NONE;

            @Override
            public BlockHitResult apply(RaycastContext innerContext, BlockPos pos) {
                //[VanillaCopy] BlockView.raycast, but optional fluid handling
                BlockState blockState = this.getBlock(world, pos);
                Vec3d start = innerContext.getStart();
                Vec3d end = innerContext.getEnd();
                VoxelShape blockShape = innerContext.getBlockShape(blockState, (BlockView) BlockViewMixin.this, pos);
                BlockHitResult blockHitResult = BlockViewMixin.this.raycastBlock(start, end, pos, blockShape, blockState);
                double d = blockHitResult == null ? Double.MAX_VALUE : innerContext.getStart().squaredDistanceTo(blockHitResult.getPos());
                double e = Double.MAX_VALUE;
                BlockHitResult fluidHitResult = null;
                if (this.handleFluids) {
                    FluidState fluidState = blockState.getFluidState();
                    VoxelShape fluidShape = innerContext.getFluidShape(fluidState, (BlockView) BlockViewMixin.this, pos);
                    fluidHitResult = fluidShape.raycast(start, end, pos);
                    e = fluidHitResult == null ? Double.MAX_VALUE : innerContext.getStart().squaredDistanceTo(fluidHitResult.getPos());
                }
                return d <= e ? blockHitResult : fluidHitResult;
            }

            private BlockState getBlock(WorldView world, BlockPos blockPos) {
                int chunkX = Pos.ChunkCoord.fromBlockCoord(blockPos.getX());
                int chunkZ = Pos.ChunkCoord.fromBlockCoord(blockPos.getZ());

                // Avoid calling into the chunk manager as much as possible through managing chunks locally
                if (this.chunkX != chunkX || this.chunkZ != chunkZ) {
                    this.chunk = world.getChunk(chunkX, chunkZ);

                    this.chunkX = chunkX;
                    this.chunkZ = chunkZ;
                }

                final Chunk chunk = this.chunk;

                // If the chunk is missing or out of bounds, assume that it is air
                if (chunk != null) {
                    // We operate directly on chunk sections to avoid interacting with BlockPos and to squeeze out as much
                    // performance as possible here
                    ChunkSection section = chunk.getSectionArray()[Pos.SectionYIndex.fromBlockCoord(chunk, blockPos.getY())];

                    // If the section doesn't exist or is empty, assume that the block is air
                    if (section != null && !section.isEmpty()) {
                        return section.getBlockState(blockPos.getX() & 15, blockPos.getY() & 15, blockPos.getZ() & 15);
                    }
                }

                return Blocks.AIR.getDefaultState();
            }
        };
    }
}
