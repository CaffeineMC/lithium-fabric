package me.jellysquid.mods.lithium.common.block.redstone;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import me.jellysquid.mods.lithium.common.block.redstone.graph.RedstoneGraph;
import me.jellysquid.mods.lithium.common.block.redstone.graph.RedstoneNode;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.*;

public class RedstoneEngine {
    private final Queue<RedstoneNode> addQueue = new ArrayDeque<>();

    private final Queue<RedstoneNode> removeQueue = new ArrayDeque<>();

    private final List<RedstoneNode> dirtyNodes = new ArrayList<>();

    private final RedstoneGraph graph;

    private final World world;

    private boolean isUpdating;

    public RedstoneEngine(World world) {
        this.world = world;
        this.graph = new RedstoneGraph(world);
    }

    private void performUpdates() {
        this.isUpdating = true;

        this.processQueues();
        this.updateWireStates();
        this.notifyListeningBlocks();

        this.dirtyNodes.clear();
        this.graph.clear();

        this.isUpdating = false;
    }

    private void processQueues() {
        while (!this.removeQueue.isEmpty()) {
            RedstoneNode node = this.removeQueue.poll();
            int power = node.getPreviousWirePower();

            this.updateLevel(node, power, true);
        }

        while (!this.addQueue.isEmpty()) {
            RedstoneNode node = this.addQueue.poll();
            int power = node.getWirePower();

            this.updateLevel(node, power, false);
        }
    }

    private void updateWireStates() {
        for (RedstoneNode info : this.dirtyNodes) {
            if (!info.isWireBlock()) {
                continue;
            }

            this.world.setBlockState(info.getPosition(), info.getBlockState().with(RedstoneWireBlock.POWER, info.getWirePower()), 2);
        }
    }

    private void notifyListeningBlocks() {
        LongSet set = this.calculatePendingBlockUpdates();
        LongIterator it = set.iterator();

        BlockPos.Mutable pos = new BlockPos.Mutable();

        while (it.hasNext()) {
            pos.set(it.nextLong());

            this.world.getBlockState(pos).neighborUpdate(this.world, pos.toImmutable(), Blocks.REDSTONE_WIRE, BlockPos.ORIGIN, false);
        }
    }

    private LongSet calculatePendingBlockUpdates() {
        LongSet set = new LongLinkedOpenHashSet();

        ListIterator<RedstoneNode> it = this.dirtyNodes.listIterator(this.dirtyNodes.size());

        while (it.hasPrevious()) {
            RedstoneNode node = it.previous();
            BlockPos pos = node.getPosition();

            for (Vec3i offset : RedstoneLogic.WIRE_UPDATE_ORDER) {
                set.add(BlockPos.asLong(pos.getX() + offset.getX(), pos.getY() + offset.getY(), pos.getZ() + offset.getZ()));
            }
        }

        return set;
    }

    public void notifyWireAdded(BlockPos pos) {
        if (this.isUpdating()) {
            return;
        }

        RedstoneNode node = this.graph.getOrCreateNode(pos);

        this.addWire(node, node.getPowerFromNeighbors());
    }

    private void addWire(RedstoneNode node, int level) {
        node.setWirePower(level);

        this.addQueue.add(node);

        this.dirtyNodes.add(node);
        this.performUpdates();
    }

    public void notifyWireRemoved(BlockPos pos) {
        if (this.isUpdating()) {
            return;
        }

        RedstoneNode node = this.graph.getOrCreateNode(pos);

        this.removeWire(node, node.getPowerFromNeighbors());
    }

    private void removeWire(RedstoneNode node, int level) {
        node.setPreviousWirePower(level);

        this.removeQueue.add(node);

        node.setWirePower(0);

        this.dirtyNodes.add(node);
        this.performUpdates();
    }

    private void updateLevel(RedstoneNode node, int power, boolean removing) {
        boolean covered = node.getAdjacentNode(Direction.UP).isFullBlock();

        for (Direction dir : RedstoneLogic.HORIZONTAL_NEIGHBORS) {
            RedstoneNode neighbor = RedstoneLogic.getNextWireInDirection(node, dir, covered);

            if (neighbor != null) {
                this.modifyLevel(neighbor, power, removing);
            }
        }
    }

    private void modifyLevel(RedstoneNode node, int power, boolean removing) {
        if (removing) {
            this.decreaseLevel(node, power);
        } else {
            this.increaseLevel(node, power);
        }
    }

    private void decreaseLevel(RedstoneNode neighbor, int power) {
        if (neighbor.traversalFlag > 0) {
            return;
        }

        neighbor.traversalFlag = 1;

        int neighborPower = neighbor.getWirePower();

        if (neighborPower != 0 && neighborPower < power) {
            neighbor.setWirePower(0);
            neighbor.setPreviousWirePower(neighborPower);

            this.dirtyNodes.add(neighbor);
            this.removeQueue.add(neighbor);
        } else if (neighborPower >= power) {
            this.addQueue.add(neighbor);
        }
    }

    private void increaseLevel(RedstoneNode neighbor, int power) {
        if (neighbor.traversalFlag > 1) {
            return;
        }

        neighbor.traversalFlag = 2;

        if (neighbor.getWirePower() + 2 <= power) {
            neighbor.setWirePower(power - 1);

            this.dirtyNodes.add(neighbor);
            this.addQueue.add(neighbor);
        }
    }

    public void clean() {
        this.graph.clear();
    }

    public boolean isUpdating() {
        return this.isUpdating;
    }

    public void notifyWireChange(BlockPos pos, int curPower) {
        if (this.isUpdating()) {
            return;
        }

        RedstoneNode node = this.graph.getOrCreateNode(pos);

        int updatedPower = node.getPowerFromNeighbors();

        if (updatedPower > curPower) {
            this.addWire(node, updatedPower);
        } else if (updatedPower < curPower) {
            this.removeWire(node, curPower);
        }

        this.clean();
    }
}
