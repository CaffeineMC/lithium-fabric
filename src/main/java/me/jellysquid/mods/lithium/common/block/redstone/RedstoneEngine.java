package me.jellysquid.mods.lithium.common.block.redstone;

import me.jellysquid.mods.lithium.common.block.redstone.graph.UpdateFlag;
import me.jellysquid.mods.lithium.common.block.redstone.graph.UpdateGraph;
import me.jellysquid.mods.lithium.common.block.redstone.graph.UpdateNode;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.Queue;

public class RedstoneEngine {
    /** The queue of nodes to brighten in FIFO order. **/
    private final Queue<UpdateNode> brightenQueue = new ArrayDeque<>();

    /** The queue of nodes to darken in FIFO order. **/
    private final Queue<UpdateNode> darkenQueue = new ArrayDeque<>();

    /** The queue of nodes to be updated in FIFO order **/
    private final Queue<UpdateNode> updateQueue = new ArrayDeque<>();

    private final UpdateGraph graph;

    /**
     * True if block updates are being performed after changes have been made to the graph. This is used to detect
     * re-entrance caused by block updates.
     */
    private boolean isUpdatingBlocks;

    public RedstoneEngine(World world) {
        this.graph = new UpdateGraph(world);
    }

    /**
     * Called when a Redstone wire block is added to the world. This creates a new node for the wire in the graph and
     * brightens it to the correct incoming power level.
     */
    public void notifyWireAdded(BlockPos pos) {
        UpdateNode node = this.graph.getOrCreateNode(pos);
        node.invalidateWorldState();

        this.updateQueue.add(node);

        this.brightenNode(node, node.calculateIncomingEffectivePower());
        this.processGraphChanges();
    }

    /**
     * Called when a Redstone wire block is removed from the world. This checks if the wire exists in the graph and then
     * enqueues all of its neighbors for darkening.
     */
    public void notifyWireRemoved(BlockPos pos, int prev) {
        UpdateNode node = this.graph.getOrCreateNode(pos);
        node.invalidateWorldState();
        node.invalidateConnections();

        this.updateQueue.add(node);

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
            this.updateQueue.add(node);
        } else if (cur < prev) {
            this.darkenNode(node, prev);
            this.updateQueue.add(node);
        }

        // This must always be called as it will be responsible for deleting the nodes we instantiated above
        this.processGraphChanges();
    }

    private void brightenNode(UpdateNode node, int level) {
        node.setCurrentWirePower(level);
        node.updateWireState();

        this.brightenQueue.add(node);
    }

    private void darkenNode(UpdateNode node, int level) {
        node.setDarkeningThreshold(level);
        node.setCurrentWirePower(RedstoneLogic.WIRE_MIN_POWER);
        node.updateWireState();

        this.darkenQueue.add(node);
    }

    private void enqueueNeighbors(UpdateNode node, int power, boolean darkening) {
        boolean canAscend = !node.getAdjacent(Direction.UP).isFullBlock();
        boolean canDescend = darkening || node.getAdjacent(Direction.DOWN).isFullBlock();

        for (Direction dir : RedstoneLogic.WIRE_NEIGHBORS_HORIZONTAL) {
            UpdateNode adj = node.getAdjacent(dir);

            // Wires directly adjacent can always be updated
            this.enqueueNode(node, adj, power, darkening);

            // If no block is covering this node, we can check in the upwards direction
            if (canAscend) {
                this.enqueueNode(node, adj.getAdjacent(Direction.UP), power, darkening);
            }

            // If the adjacent block is non-full, we can check in the downwards direction
            if (canDescend && !adj.isFullBlock()) {
                this.enqueueNode(node, adj.getAdjacent(Direction.DOWN), power, darkening);
            }
        }
    }

    private void enqueueNode(UpdateNode node, UpdateNode adj, int power, boolean darkening) {
        if (!adj.isWireBlock()) {
            return;
        }

        boolean result;

        if (darkening) {
            result = this.enqueueNodeForDarkening(adj, power);
        } else {
            result = this.enqueueNodeForBrightening(adj, power);
        }

        if (result) {
            node.addConnection(adj);
        }
    }

    private boolean enqueueNodeForDarkening(UpdateNode node, int power) {
        if (!node.checkAndMarkFlag(UpdateFlag.DARKENED)) {
            return false;
        }

        int neighborPower = node.getCurrentWirePower();

        if (neighborPower > RedstoneLogic.WIRE_MIN_POWER && neighborPower < power) {
            this.darkenNode(node, neighborPower);

            return true;
        }

        if (neighborPower >= power - RedstoneLogic.WIRE_POWER_LOSS_PER_BLOCK) {
            this.brightenQueue.add(node);
        }

        return false;
    }

    private boolean enqueueNodeForBrightening(UpdateNode node, int power) {
        if (!node.checkAndMarkFlag(UpdateFlag.BRIGHTENED)) {
            return false;
        }

        if (node.getCurrentWirePower() + RedstoneLogic.WIRE_POWER_LOSS_PER_BLOCK < power) {
            this.brightenNode(node, power - RedstoneLogic.WIRE_POWER_LOSS_PER_BLOCK);

            return true;
        }

        return false;
    }

    private void processBlockUpdates() {
        // Detect if re-entrance has occurred, and if so, exit to allow the stack to unwind
        if (this.isUpdatingBlocks) {
            return;
        }

        this.isUpdatingBlocks = true;

        while (!this.updateQueue.isEmpty()) {
            UpdateNode node = this.updateQueue.remove();

            // If we have already processed this element in the queue, skip it
            // This is necessary to prevent nodes which link to themselves from updating forever
            if (!node.checkAndMarkFlag(UpdateFlag.WIRE_UPDATED)) {
                continue;
            }

            // Notify each wire neighbor of the change in power state
            for (Direction dir1 : RedstoneLogic.BLOCK_NEIGHBOR_UPDATE_ORDER) {
                UpdateNode adj1 = node.getAdjacent(dir1);

                if (adj1.checkAndMarkFlag(UpdateFlag.NEIGHBOR_UPDATED)) {
                    adj1.update(node, dir1, true);
                }

                // Notify any the surrounding blocks to the immediate neighbors of the wire
                // This is necessary to maintain quasi-connectivity and block update detectors
                for (Direction dir2 : RedstoneLogic.BLOCK_NEIGHBOR_UPDATE_ORDER) {
                    UpdateNode adj2 = adj1.getAdjacent(dir2);

                    // Only update neighbors needed for quasi-connectivity once
                    if (adj2.checkAndMarkFlag(UpdateFlag.QUASI_NEIGHBOR_UPDATED)) {
                        adj2.update(adj1, dir2, false);
                    }
                }
            }

            // Check if any of the updates we just performed have resulted in this wire being invalidated
            if (!node.isWireAtValidLocation()) {
                // We're no longer valid, so we will be dropped as an item and removed from the world
                // This will cause re-entrance to the engine upon the block removal notification being made, which will
                // delete this node's connections to other nodes in the graph
                node.destroyWire();
            }

            // Add any connections to the end of the queue. This results in updates being ordered by logical distance
            // from the origin node
            this.updateQueue.addAll(node.getConnections());
        }

        this.graph.clear();

        this.isUpdatingBlocks = false;
    }

    private void processGraphChanges() {
        this.processQueuedDarkenings();
        this.processQueuedBrightenings();

        this.resetGraphTraversalState();

        this.processBlockUpdates();
    }

    private void resetGraphTraversalState() {
        for (UpdateNode node : this.graph) {
            node.clearFlags();
        }
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
