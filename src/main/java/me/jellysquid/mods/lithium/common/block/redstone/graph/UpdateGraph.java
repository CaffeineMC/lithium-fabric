package me.jellysquid.mods.lithium.common.block.redstone.graph;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Iterator;

public class UpdateGraph implements Iterable<UpdateNode> {
    private final World world;

    private final Long2ObjectLinkedOpenHashMap<UpdateNode> nodesByPosition = new Long2ObjectLinkedOpenHashMap<>();

    public UpdateGraph(World world) {
        this.world = world;
    }

    public UpdateNode get(BlockPos pos) {
        return this.nodesByPosition.get(pos.asLong());
    }

    public UpdateNode getOrCreateNode(BlockPos pos) {
        long id = pos.asLong();

        UpdateNode info = this.nodesByPosition.get(id);

        if (info == null) {
            this.nodesByPosition.put(id, info = new UpdateNode(this, pos));
        }

        return info;
    }

    public World getWorld() {
        return this.world;
    }

    @Override
    public Iterator<UpdateNode> iterator() {
        return this.nodesByPosition.values().iterator();
    }

    public void clear() {
        this.nodesByPosition.clear();
    }
}
