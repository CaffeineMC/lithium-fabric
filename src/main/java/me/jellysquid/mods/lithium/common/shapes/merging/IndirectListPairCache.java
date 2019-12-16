package me.jellysquid.mods.lithium.common.shapes.merging;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.jellysquid.mods.lithium.common.LithiumMod;

import java.util.ArrayDeque;
import java.util.Queue;

public class IndirectListPairCache {
    private static final ThreadLocal<Int2ObjectOpenHashMap<Queue<IndirectListPair>>> local = ThreadLocal.withInitial(Int2ObjectOpenHashMap::new);

    private static boolean ENABLE = true;

    public static void init() {
        ENABLE = LithiumMod.CONFIG.physics.useAllocationPoolingForVertexListMerging;
    }

    public static IndirectListPair create(DoubleList a, DoubleList b, boolean flag1, boolean flag2) {
        if (ENABLE) {
            int size = a.size() + b.size();

            IndirectListPair list;

            Queue<IndirectListPair> queue = local.get().get(size);

            if (queue != null && queue.size() > 0) {
                list = queue.remove();
                list.clear();
            } else {
                list = new IndirectListPair(size);
            }

            list.initBest(a, b, flag1, flag2);

            return list;
        } else {
            IndirectListPair list = new IndirectListPair(a.size() + b.size());
            list.initBest(a, b, flag1, flag2);

            return list;
        }
    }

    public static void release(IndirectListPair listPair) {
        if (ENABLE) {
            Queue<IndirectListPair> queue = local.get().computeIfAbsent(listPair.getSize(), unused -> new ArrayDeque<>());

            if (queue.size() > 10) {
                return;
            }

            queue.add(listPair);
        }
    }
}
