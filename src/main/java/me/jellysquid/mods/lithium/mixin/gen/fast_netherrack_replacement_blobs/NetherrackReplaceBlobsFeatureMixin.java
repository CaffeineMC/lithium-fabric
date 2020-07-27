package me.jellysquid.mods.lithium.mixin.gen.fast_netherrack_replacement_blobs;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import it.unimi.dsi.fastutil.longs.Long2ReferenceLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.NetherrackReplaceBlobsFeature;
import net.minecraft.world.gen.feature.NetherrackReplaceBlobsFeatureConfig;

@Mixin(NetherrackReplaceBlobsFeature.class)
public abstract class NetherrackReplaceBlobsFeatureMixin {
	private static final Long2ReferenceLinkedOpenHashMap<LongArrayList> DELTA_CACHE = new Long2ReferenceLinkedOpenHashMap<>();

	static {
		DELTA_CACHE.defaultReturnValue(null);
	}

	// Finds start position
	@Shadow
	protected static BlockPos method_27107(WorldAccess world, BlockPos.Mutable mutable, Block block) {
		return null;
	}

	// Generates the size of this blob
	@Shadow
	protected static Vec3i method_27108(Random random, NetherrackReplaceBlobsFeatureConfig config) {
		return null;
	}

	/**
	 * @reason Faster implementation which utilizes a cache of delta positions instead of iterating outwards many times.
     * This saves many CPU cycles as this feature is called 100 times per chunk in the Basalt Deltas biome.
	 * @author SuperCoder79
	 */
	@Overwrite
	public boolean generate(ServerWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, NetherrackReplaceBlobsFeatureConfig config) {
		// [VanillaCopy] NetherrackReplaceBlobsFeature#generate

	    Block targetBlock = config.target.getBlock();

		// Get start position
		BlockPos startPos = method_27107(world, blockPos.mutableCopy().method_27158(Direction.Axis.Y, 1, world.getHeight() - 1), targetBlock);

		// Exit early if we couldn't create the start position.
		if (startPos == null) {
			return false;
		} else {
			boolean didPlace = false;

			// Get the delta positions from size vector
            LongArrayList deltas = getDeltas(method_27108(random, config));
			BlockPos.Mutable mutable = new BlockPos.Mutable();

			// Iterate through the delta positions and attempt to place each one.
			for (long delta : deltas) {
				// Add the delta to the start position
				mutable.set(startPos, BlockPos.unpackLongX(delta), BlockPos.unpackLongY(delta), BlockPos.unpackLongZ(delta));

				// If the block here is the target, replace.
				BlockState hereState = world.getBlockState(mutable);
				if (hereState.isOf(targetBlock)) {
					world.setBlockState(mutable, config.state, 3);
					didPlace = true;
				}
			}

			return didPlace;
		}
	}

	private static LongArrayList getDeltas(Vec3i size) {
	    // Attempt to retrieve values from the cache
		long packed = BlockPos.asLong(size.getX(), size.getY(), size.getZ());
        LongArrayList deltas = DELTA_CACHE.get(packed);

		// Cache hit, return value
		if (deltas != null) {
			return deltas;
		}

		// Cache miss, compute and store

        // Get the largest of the sides
		int maxExtent = Math.max(size.getX(), Math.max(size.getY(), size.getZ()));

		// Iterate outwards and add the deltas that are within the max extent
		deltas = new LongArrayList();
		for (BlockPos pos : BlockPos.iterateOutwards(BlockPos.ORIGIN, size.getX(), size.getY(), size.getZ())) {
			BlockPos delta = pos.toImmutable();

			// Only add the positions whose distance from the origin are less than the maxExtent, creating a sphere.
            // In vanilla, this is run every time the feature generates but we can cache it for a large benefit.
			if (!(delta.getManhattanDistance(BlockPos.ORIGIN) > maxExtent)) {
				deltas.add(delta.asLong());
			}
		}

		// Store values and return
		DELTA_CACHE.put(packed, deltas);

		// Ensure that the cache doesn't overflow by removing entries after a certain threshold.
        // In this case, the chosen threshold is 125 as vanilla only creates blobs that have a size from 3-7 on all axes.
		if (DELTA_CACHE.size() > 125) {
		    DELTA_CACHE.removeLast();
        }

		return deltas;

	}
}
