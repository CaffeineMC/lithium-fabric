package me.jellysquid.mods.lithium.common.cached_blockpos_iteration;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.util.math.BlockPos;

import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 2No2Name, original implemenation by SuperCoder7979 and Gegy1000
 */
public class IterateOutwardsCache {
    //POS_ZERO must not be replaced with BlockPos.ORIGIN, otherwise iterateOutwards at BlockPos.ORIGIN will not use the cache
    public static final BlockPos POS_ZERO = new BlockPos(0,0,0);


    private final ConcurrentHashMap<Long, LongArrayList> table;
    private final int capacity;
    private final Random random;

    public IterateOutwardsCache(int capacity) {
        this.capacity = capacity;
        this.table = new ConcurrentHashMap<>(31);
        this.random = new Random();
    }

    private void fillPositionsWithIterateOutwards(LongList entry, int xRange, int yRange, int zRange) {
        // Add all positions to the cached list
        for (BlockPos pos : BlockPos.iterateOutwards(POS_ZERO, xRange, yRange, zRange)) {
            entry.add(pos.asLong());
        }
    }

    public LongList getOrCompute(int xRange, int yRange, int zRange) {
        long key = BlockPos.asLong(xRange, yRange, zRange);

        LongArrayList entry = this.table.get(key);
        if (entry != null) {
            return entry;
        }

        // Cache miss: compute and store
        entry = new LongArrayList(128);

        this.fillPositionsWithIterateOutwards(entry, xRange, yRange, zRange);

        //decrease the array size, as of now it won't be modified anymore anyways
        entry.trim();

        //this might overwrite an entry as the same entry could have been computed and added during this thread's computation
        //we do not use computeIfAbsent, as it can delay other threads for too long
        Object previousEntry = this.table.put(key, entry);


        if (previousEntry == null && this.table.size() > this.capacity) {
            //prevent a memory leak by randomly removing about 1/8th of the elements when the exceed the desired capacity is exceeded
            final Iterator<Long> iterator = this.table.keySet().iterator();
            //prevent an unlikely infinite loop caused by another thread filling the table concurrently using counting
            for (int i = -this.capacity; iterator.hasNext() && i < 5; i++) {
                Long key2 = iterator.next();
                //random is not threadsafe, but it doesn't matter here, because we don't need quality random numbers
                if (this.random.nextInt(8) == 0 && key2 != key) {
                    iterator.remove();
                }
            }
        }

        return entry;
    }
}
