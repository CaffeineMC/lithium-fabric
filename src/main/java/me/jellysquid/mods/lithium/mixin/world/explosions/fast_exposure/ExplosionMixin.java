package me.jellysquid.mods.lithium.mixin.world.explosions.fast_exposure;

import it.unimi.dsi.fastutil.ints.Int2ByteMap;
import me.jellysquid.mods.lithium.common.util.Pos;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BiFunction;

/**
 * Optimizations for Explosions: Reduce workload for exposure calculations
 * @author Crosby
 */
@Mixin(Explosion.class)
public class ExplosionMixin {
    @Unique private static final BlockHitResult MISS = BlockHitResult.createMissed(null, null, null);

    @Unique private static BiFunction<RaycastContext, BlockPos, BlockHitResult> hitFactory;

    // The chunk coordinate of the most recently stepped through block.
    @Unique private static int prevChunkX = Integer.MIN_VALUE;
    @Unique private static int prevChunkZ = Integer.MIN_VALUE;

    // The chunk belonging to prevChunkPos.
    @Unique private static Chunk prevChunk;

    /**
     * Skip exposure calculations (used for calculating velocity) on entities which get discarded when blown up. (For
     * example: dropped items & experience orbs)
     * This doesn't improve performance when {@code world.explosions.cache_exposure} is active, but it doesn't hurt to
     * keep.
     * @author Crosby
     */
    @Inject(method = "getExposure", at = @At("HEAD"), cancellable = true)
    private static void skipDeadEntities(Vec3d source, Entity entity, CallbackInfoReturnable<Float> cir) {
        Entity.RemovalReason removalReason = entity.getRemovalReason();
        if (removalReason != null && removalReason.shouldDestroy()) cir.setReturnValue(0f);
    }

    /**
     * Since the hit factory lambda needs to capture the {@code World}, we allocate it once and reuse it.
     * @author Crosby
     */
    @Inject(method = "getExposure", at = @At("HEAD"))
    private static void allocateCapturingLambda(Vec3d source, Entity entity, CallbackInfoReturnable<Float> cir) {
        hitFactory = simpleRaycast(entity.getWorld());
    }

    /**
     * We don't actually care where a raycast miss happens, so we can return a constant object to prevent a useless
     * object allocation.
     * @author Crosby
     */
    @Redirect(method = "getExposure", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;raycast(Lnet/minecraft/world/RaycastContext;)Lnet/minecraft/util/hit/BlockHitResult;"))
    private static BlockHitResult optimizeRaycast(World instance, RaycastContext context) {
        return BlockView.raycast(context.getStart(), context.getEnd(), context, hitFactory, ctx -> MISS);
    }

    /**
     * Since we don't care about fluid handling, hit direction, and all that other fluff, we can massively simplify the
     * work done in raycasts.
     * @author Crosby
     */
    @Unique
    private static BiFunction<RaycastContext, BlockPos, BlockHitResult> simpleRaycast(World world) {
        return (context, blockPos) -> {
            BlockState blockState = getBlock(world, blockPos);

            return blockState.getCollisionShape(world, blockPos).raycast(context.getStart(), context.getEnd(), blockPos);
        };
    }

    @Unique
    private static BlockState getBlock(World world, BlockPos blockPos) {
        int chunkX = Pos.ChunkCoord.fromBlockCoord(blockPos.getX());
        int chunkZ = Pos.ChunkCoord.fromBlockCoord(blockPos.getZ());

        // Avoid calling into the chunk manager as much as possible through managing chunks locally
        if (prevChunkX != chunkX || prevChunkZ != chunkZ) {
            prevChunk = world.getChunk(chunkX, chunkZ);

            prevChunkX = chunkX;
            prevChunkZ = chunkZ;
        }

        final Chunk chunk = prevChunk;

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
}
