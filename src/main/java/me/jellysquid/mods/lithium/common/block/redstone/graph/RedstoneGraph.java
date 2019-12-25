package me.jellysquid.mods.lithium.common.block.redstone.graph;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import java.util.Iterator;

public class RedstoneGraph implements Iterable<RedstoneNode> {
    private final Long2ObjectLinkedOpenHashMap<RedstoneNode> nodes = new Long2ObjectLinkedOpenHashMap<>();

    public RedstoneNode getNode(BlockView world, BlockPos pos) {
        RedstoneNode info = this.nodes.get(pos.asLong());

        if (info == null) {
            this.nodes.put(pos.asLong(), info = new RedstoneNode(this, world, pos));
        }

        return info;
    }

    @Override
    public Iterator<RedstoneNode> iterator() {
        return this.nodes.values().iterator();
    }

    public void clear() {
        this.nodes.clear();
    }
}
