package me.jellysquid.mods.lithium.mixin.entity.simple_entity_block_collisions;

import me.jellysquid.mods.lithium.common.cache.EntityChunkCache;
import me.jellysquid.mods.lithium.common.entity.ExtendedEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.CuboidBlockIterator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.ViewableWorld;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;
import java.util.stream.Stream;

@Mixin(ViewableWorld.class)
public interface MixinViewableWorld {
    @Shadow
    Stream<VoxelShape> getCollisionShapes(Entity entity_1, Box box_1, Set<Entity> set_1);

    @Shadow
    ChunkStatus getLeastChunkStatusForCollisionCalculation();

    @Shadow
    Chunk getChunk(int x, int z, ChunkStatus status, boolean var4);

    /**
     * Uses a simpler and much faster collision detection algo. If we run into shapes our system can't handle, we'll
     * fallback to the vanilla algo. This provides a massive improvement in some situations, such as ticking items on
     * the ground.
     *
     * @author JellySquid
     */
    @Overwrite
    default boolean doesNotCollide(Entity entity, Box box, Set<Entity> ignore) {
        EntityChunkCache entityChunkCache = null;

        if (entity != null) {
            entityChunkCache = ((ExtendedEntity) entity).getEntityChunkCache();
        }

        int minX = MathHelper.floor(box.minX - 1.0E-7D) - 1;
        int maxX = MathHelper.floor(box.maxX + 1.0E-7D) + 1;
        int minY = MathHelper.floor(box.minY - 1.0E-7D) - 1;
        int maxY = MathHelper.floor(box.maxY + 1.0E-7D) + 1;
        int minZ = MathHelper.floor(box.minZ - 1.0E-7D) - 1;
        int maxZ = MathHelper.floor(box.maxZ + 1.0E-7D) + 1;

        final CuboidBlockIterator cuboidIterator = new CuboidBlockIterator(minX, minY, minZ, maxX, maxY, maxZ);

        BlockPos.Mutable pos = new BlockPos.Mutable();
        boolean useFallback = false; // If set, we need to abort and go through the slower route.
        boolean ret = false; // The calculated result so far

        while (!ret && cuboidIterator.step()) {
            int x = cuboidIterator.getX();
            int y = cuboidIterator.getY();
            int z = cuboidIterator.getZ();

            pos.set(x, y, z);

            BlockState state;

            if (entityChunkCache != null) {
                state = entityChunkCache.getBlockState(x, y, z);
            } else {
                Chunk chunk = this.getChunk(x >> 4, z >> 4, this.getLeastChunkStatusForCollisionCalculation(), false);

                if (chunk == null) {
                    state = Blocks.AIR.getDefaultState();
                } else {
                    state = chunk.getBlockState(pos);
                }
            }

            VoxelShape shape = state.getCollisionShape((ViewableWorld) this, pos);

            if (shape == VoxelShapes.fullCube()) {
                ret = box.intersects(x, y, z, x + 1.0D, y + 1.0D, z + 1.0D);
            } else if (shape != VoxelShapes.empty()) {
                useFallback = true;
                break;
            }
        }

        // Abort! something is too complex for our simple test to handle...
        if (useFallback) {
            return this.getCollisionShapes(entity, box, ignore).allMatch(VoxelShape::isEmpty);
        }

        return !ret;
    }
}
