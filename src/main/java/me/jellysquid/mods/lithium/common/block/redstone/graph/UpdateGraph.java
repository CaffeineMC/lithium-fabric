package me.jellysquid.mods.lithium.common.block.redstone.graph;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import me.jellysquid.mods.lithium.common.block.redstone.RedstoneBlockAccess;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Iterator;

public class UpdateGraph implements Iterable<UpdateNode> {
    private final World world;

    private final Long2ObjectOpenHashMap<UpdateNode> nodesByPosition = new Long2ObjectOpenHashMap<>();

    private final RedstoneBlockAccess blockAccess;

    public UpdateGraph(World world) {
        this.world = world;
        this.blockAccess = new RedstoneBlockAccess(world);
    }

    public UpdateNode get(BlockPos pos) {
        return this.nodesByPosition.get(pos.asLong());
    }

    public UpdateNode getOrCreateNode(BlockPos pos) {
        return this.nodesByPosition.computeIfAbsent(pos.asLong(), id -> new UpdateNode(this, BlockPos.fromLong(id)));
    }

    public World getWorld() {
        return this.world;
    }

    public RedstoneBlockAccess getBlockAccess() {
        return this.blockAccess;
    }

    @Override
    public Iterator<UpdateNode> iterator() {
        return this.nodesByPosition.values().iterator();
    }

    public void clear() {
        this.nodesByPosition.clear();
        this.blockAccess.clear();
    }
}
