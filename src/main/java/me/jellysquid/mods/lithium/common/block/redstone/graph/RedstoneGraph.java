package me.jellysquid.mods.lithium.common.block.redstone.graph;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Iterator;

public class RedstoneGraph implements Iterable<RedstoneNode> {
    private final World world;

    private final Long2ObjectLinkedOpenHashMap<RedstoneNode> nodesByPosition = new Long2ObjectLinkedOpenHashMap<>();

    public RedstoneGraph(World world) {
        this.world = world;
    }

    public RedstoneNode getNodeByPosition(BlockPos pos) {
        long id = pos.asLong();

        RedstoneNode info = this.nodesByPosition.get(id);

        if (info == null) {
            this.nodesByPosition.put(id, info = new RedstoneNode(this, pos));
        }

        return info;
    }

    public World getWorld() {
        return this.world;
    }

    @Override
    public Iterator<RedstoneNode> iterator() {
        return this.nodesByPosition.values().iterator();
    }

    public void clear() {
        this.nodesByPosition.clear();
    }
}
