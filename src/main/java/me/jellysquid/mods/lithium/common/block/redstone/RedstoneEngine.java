package me.jellysquid.mods.lithium.common.block.redstone;

import me.jellysquid.mods.lithium.common.block.redstone.graph.UpdateGraph;
import me.jellysquid.mods.lithium.common.block.redstone.graph.UpdateNode;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.Queue;

public class RedstoneEngine {
    private final Queue<UpdateNode> brightenQueue = new ArrayDeque<>();

    private final Queue<UpdateNode> darkenQueue = new ArrayDeque<>();

    private final UpdateGraph graph;

    private final World world;

    private boolean isUpdatingGraph;

    public RedstoneEngine(World world) {
        this.world = world;
        this.graph = new UpdateGraph(world);
    }

    /**
     * Called when a Redstone wire block is added to the world. This creates a new node for the wire in the graph and
     * brightens it to the correct incoming power level.
     */
    public void notifyWireAdded(BlockPos pos) {
        UpdateNode node = this.graph.getOrCreateNode(pos);
        node.invalidateCache();

        this.brightenNode(node, node.calculateIncomingEffectivePower());
        this.processGraphChanges();
    }

    /**
     * Called when a Redstone wire block is removed from the world. This checks if the wire exists in the graph and then
     * enqueues all of its neighbors for darkening.
     */
    public void notifyWireRemoved(BlockPos pos, int prev) {
        UpdateNode node = this.graph.getOrCreateNode(pos);
        node.invalidateCache();

        this.enqueueNeighbors(node, prev, true);
        this.processGraphChanges();
    }

    /**
     * Called when a Redstone wire block is notified of an update.
     */
    public void notifyWireNeighborChanged(BlockPos pos, int prev) {
        UpdateNode node = this.graph.get(pos);

        if (node != null) {
            if (node.getCurrentWirePower() == prev) {
                return;
            }
        } else {
            node = this.graph.getOrCreateNode(pos);
        }

        int cur = node.calculateIncomingEffectivePower();

        if (cur > prev) {
            this.brightenNode(node, cur);
        } else if (cur < prev) {
            this.darkenNode(node, prev);
        }

        // This must always be called as it will be responsible for deleting the nodes we instantiated above
        this.processGraphChanges();
    }

    private void brightenNode(UpdateNode node, int level) {
        node.setCurrentWirePower(level);

        this.brightenQueue.add(node);

        this.processNodeChange(node);
    }

    private void darkenNode(UpdateNode node, int level) {
        node.setDarkeningThreshold(level);
        node.setCurrentWirePower(RedstoneLogic.WIRE_MIN_POWER);

        this.darkenQueue.add(node);

        this.processNodeChange(node);
    }

    private void enqueueNeighbors(UpdateNode node, int power, boolean darkening) {
        boolean canAscend = !node.fetchAdjacentNode(Direction.UP).isFullBlock();
        boolean canDescend = darkening || node.fetchAdjacentNode(Direction.DOWN).isFullBlock();

        for (Direction dir : RedstoneLogic.WIRE_NEIGHBORS_HORIZONTAL) {
            UpdateNode adj = node.fetchAdjacentNode(dir);

            // Wires directly adjacent can always be updated
            this.enqueueNode(adj, power, darkening);

            // If no block is covering this node, we can check in the upwards direction
            if (canAscend) {
                this.enqueueNode(adj.fetchAdjacentNode(Direction.UP), power, darkening);
            }

            // If the adjacent block is non-full, we can check in the downwards direction
            if (canDescend && !adj.isFullBlock()) {
                this.enqueueNode(adj.fetchAdjacentNode(Direction.DOWN), power, darkening);
            }
        }
    }

    private void enqueueNode(UpdateNode node, int power, boolean darkening) {
        if (node.isWireBlock()) {
            if (darkening) {
                this.enqueueNodeForDarkening(node, power);
            } else {
                this.enqueueNodeForBrightening(node, power);
            }
        }
    }

    private void enqueueNodeForDarkening(UpdateNode node, int power) {
        if (node.getTraversalFlag() <= 0) {
            node.setTraversalFlag(1);

            int neighborPower = node.getCurrentWirePower();

            if (neighborPower > RedstoneLogic.WIRE_MIN_POWER && neighborPower < power) {
                this.darkenNode(node, neighborPower);
            } else if (neighborPower >= power - RedstoneLogic.WIRE_POWER_LOSS_PER_BLOCK) {
                this.brightenQueue.add(node);
            }
        }
    }

    private void enqueueNodeForBrightening(UpdateNode node, int power) {
        if (node.getTraversalFlag() <= 1) {
            node.setTraversalFlag(2);

            if (node.getCurrentWirePower() + RedstoneLogic.WIRE_POWER_LOSS_PER_BLOCK < power) {
                this.brightenNode(node, power - RedstoneLogic.WIRE_POWER_LOSS_PER_BLOCK);
            }
        }
    }

    private void processNodeChange(UpdateNode node) {
        node.updateWireState();
        node.getBlockState().neighborUpdate(this.world, node.getPosition(), Blocks.REDSTONE_WIRE, node.getPosition(), false);

        // TODO: This still results in an unnecessary amount of updates between wire block changes...
        for (Vec3i offset : RedstoneLogic.WIRE_NEIGHBOR_UPDATE_ORDER) {
            // TODO: Use BlockPos.Mutable
            BlockPos adj = node.getPosition().add(offset);
            BlockState state = this.world.getBlockState(adj);

            // TODO: Avoid this check by making sure we never even try updating redstone wire again
            if (state.getBlock() != Blocks.REDSTONE_WIRE) {
                state.neighborUpdate(this.world, adj, Blocks.REDSTONE_WIRE, node.getPosition(), false);
            }
        }
    }

    private void processGraphChanges() {
        if (this.isUpdatingGraph) {
            return;
        }

        this.isUpdatingGraph = true;

        while (!this.darkenQueue.isEmpty() || !this.brightenQueue.isEmpty()) {
            this.processQueuedDarkenings();
            this.processQueuedBrightenings();
        }

        this.graph.clear();

        this.isUpdatingGraph = false;
    }

    private void processQueuedDarkenings() {
        while (!this.darkenQueue.isEmpty()) {
            UpdateNode node = this.darkenQueue.poll();

            this.enqueueNeighbors(node, node.getDarkeningThreshold(), true);
        }
    }

    private void processQueuedBrightenings() {
        while (!this.brightenQueue.isEmpty()) {
            UpdateNode node = this.brightenQueue.poll();

            this.enqueueNeighbors(node, node.getCurrentWirePower(), false);
        }
    }
}
