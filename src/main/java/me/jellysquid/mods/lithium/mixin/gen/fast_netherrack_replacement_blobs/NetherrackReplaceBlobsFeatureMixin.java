package me.jellysquid.mods.lithium.mixin.gen.fast_netherrack_replacement_blobs;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongListIterator;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.NetherrackReplaceBlobsFeature;
import net.minecraft.world.gen.feature.NetherrackReplaceBlobsFeatureConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Random;

@Mixin(NetherrackReplaceBlobsFeature.class)
public abstract class NetherrackReplaceBlobsFeatureMixin {
    private static final Cache SHAPE_CACHE = new Cache(128);

    // Finds start position
    @Shadow
    private static BlockPos method_27107(WorldAccess world, BlockPos.Mutable mutable, Block block) {
        throw new UnsupportedOperationException();
    }

    // Generates the size of this blob
    @Shadow
    private static Vec3i method_27108(Random random, NetherrackReplaceBlobsFeatureConfig config) {
        throw new UnsupportedOperationException();
    }

    /**
     * @reason Faster implementation which utilizes a cache of delta positions instead of iterating outwards many times.
     * This saves many CPU cycles as this feature is called 100 times per chunk in the Basalt Deltas biome.
     * @author SuperCoder79
     */
    @Overwrite
    public boolean generate(ServerWorldAccess world, StructureAccessor structures, ChunkGenerator generator, Random random, BlockPos origin, NetherrackReplaceBlobsFeatureConfig config) {
        // [VanillaCopy] NetherrackReplaceBlobsFeature#generate

        Block targetBlock = config.target.getBlock();

        // Get start position
        BlockPos startPos = method_27107(world, origin.mutableCopy().method_27158(Direction.Axis.Y, 1, world.getHeight() - 1), targetBlock);

        // Exit early if we couldn't create the start position.
        if (startPos == null) {
            return false;
        }

        // Get the delta positions from size vector
        Vec3i size = method_27108(random, config);
        LongList shape = SHAPE_CACHE.getOrCompute(size);

        BlockPos.Mutable mutable = new BlockPos.Mutable();

        boolean didPlace = false;

        // Iterate through the delta positions and attempt to place each one.
        LongListIterator iterator = shape.iterator();

        // the first value is the size key: skip it
        iterator.nextLong();

        while (iterator.hasNext()) {
            long offset = iterator.nextLong();

            // Add the delta to the start position
            mutable.set(startPos, BlockPos.unpackLongX(offset), BlockPos.unpackLongY(offset), BlockPos.unpackLongZ(offset));

            // If the block here is the target, replace.
            if (world.getBlockState(mutable).isOf(targetBlock)) {
                world.setBlockState(mutable, config.state, 3);
                didPlace = true;
            }
        }

        return didPlace;
    }

    @SuppressWarnings("MixinInnerClass")
    private static class Cache {
        private final LongList[] table;
        private final int mask;

        Cache(int capacity) {
            capacity = MathHelper.smallestEncompassingPowerOfTwo(capacity);
            this.mask = capacity - 1;

            this.table = new LongList[capacity];
        }

        void addPositionsTo(Vec3i size, LongList entry) {
            // Get the largest of the sides
            int maxExtent = Math.max(size.getX(), Math.max(size.getY(), size.getZ()));

            // Iterate outwards and add the deltas that are within the max extent
            for (BlockPos pos : BlockPos.iterateOutwards(BlockPos.ORIGIN, size.getX(), size.getY(), size.getZ())) {
                // Only add the positions whose distance from the origin are less than the maxExtent, creating a sphere.
                // In vanilla, this is run every time the feature generates but we can cache it for a large benefit.
                if (pos.getManhattanDistance(BlockPos.ORIGIN) > maxExtent) {
                    break;
                }

                entry.add(pos.asLong());
            }
        }

        LongList getOrCompute(Vec3i size) {
            long key = BlockPos.asLong(size.getX(), size.getY(), size.getZ());
            int idx = hash(key) & this.mask;

            LongList entry = this.table[idx];
            if (entry != null && entry.getLong(0) == key) {
                // cache hit: first value in entry matches our key
                return entry;
            }

            // cache miss: compute and store
            entry = new LongArrayList(128);

            // first value in the entry is the key
            // we do this to avoid race conditions by having two separate arrays for the keys and values
            entry.add(key);

            this.addPositionsTo(size, entry);

            this.table[idx] = entry;

            return entry;
        }

        private static int hash(long key) {
            return (int) HashCommon.mix(key);
        }
    }
}
