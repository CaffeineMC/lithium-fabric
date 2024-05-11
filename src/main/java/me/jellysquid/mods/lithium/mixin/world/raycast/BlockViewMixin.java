package me.jellysquid.mods.lithium.mixin.world.raycast;

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
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.BiFunction;
import java.util.function.Function;

@SuppressWarnings("ShadowModifiers")
@Mixin(BlockView.class)
public interface BlockViewMixin {

    @Shadow
    BlockState getBlockState(BlockPos pos);

    @Shadow
    @Nullable BlockHitResult raycastBlock(Vec3d start, Vec3d end, BlockPos pos, VoxelShape shape, BlockState state);

    @Shadow
    static <T, C> T raycast(Vec3d start, Vec3d end, C context, BiFunction<C, BlockPos, T> blockHitFactory, Function<C, T> missFactory) {throw new AssertionError();}

    @Shadow
    public BlockHitResult method_17743(RaycastContext par1, BlockPos par2);

    @Shadow
    public static BlockHitResult method_17746(RaycastContext par1) { throw new AssertionError();}

    /**
     * @author 2No2Name
     * @reason Get rid of unnecessary lambda allocation
     */
    @Overwrite
    default BlockHitResult raycast(RaycastContext context) {
        return raycast(context.getStart(), context.getEnd(), context, this instanceof WorldView ? this.blockHitFactory(context) : this::method_17743, BlockViewMixin::method_17746);
    }

    @Unique
    private BiFunction<RaycastContext, BlockPos, BlockHitResult> blockHitFactory(RaycastContext context) {
        return new BiFunction<>() {
            int chunkX = Integer.MIN_VALUE, chunkZ = Integer.MIN_VALUE;
            Chunk chunk = null;
            final boolean handleFluids = ((RaycastContextAccessor) context).getFluidHandling() != RaycastContext.FluidHandling.NONE;

            @Override
            public BlockHitResult apply(RaycastContext innerContext, BlockPos pos) {
                //[VanillaCopy] BlockView.raycast, but optional fluid handling
                BlockState blockState = this.getBlock((WorldView) BlockViewMixin.this, pos);
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
                if (world.isOutOfHeightLimit(blockPos.getY())) {
                    return Blocks.VOID_AIR.getDefaultState();
                }
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
