package me.jellysquid.mods.lithium.common.block.redstone.graph;

import me.jellysquid.mods.lithium.common.block.redstone.RedstoneLogic;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class UpdateNode {
    private final UpdateGraph graph;
    private final BlockPos pos;

    private final UpdateNode[] adjacentNodes = new UpdateNode[6];

    private BlockState state;
    private UpdateNodeBlockType type;

    private byte currentWirePower = 0;
    private byte darkeningThreshold = -1;

    private byte traversalFlag;

    UpdateNode(UpdateGraph graph, BlockPos pos) {
        this.graph = graph;
        this.pos = pos.toImmutable();

        this.invalidateCache();
    }

    /**
     * Returns the node directly adjacent from this node in the specified direction. This will make use of a cache
     * to make traversing the node graph extremely quick. If a node is not in the cache, it will be looked for in the
     * graph using a slower method. If the graph doesn't contain the node, it will be initialized and linked to this
     * node in the opposite direction.
     */
    public UpdateNode fetchAdjacentNode(Direction dir) {
        UpdateNode adj = this.adjacentNodes[dir.ordinal()];

        if (adj == null) {
            adj = this.graph.getOrCreateNode(this.pos.offset(dir));
            adj.adjacentNodes[dir.getOpposite().ordinal()] = this; // Link the node back to us

            return this.adjacentNodes[dir.ordinal()] = adj;
        }

        return adj;
    }

    /**
     * Returns true if this node represents a wire block, otherwise false.
     */
    public boolean isWireBlock() {
        return this.type == UpdateNodeBlockType.WIRE;
    }

    /**
     * Returns true if this node represents a simple full block, otherwise false. If this node is a wire, it will
     * always return false.
     */
    public boolean isFullBlock() {
        return this.type == UpdateNodeBlockType.FULL_BLOCK;
    }

    /**
     * Returns the block state representing this node in the world.
     */
    public BlockState getBlockState() {
        return this.state;
    }

    /**
     * Returns the current power of this wire node. If the node is not a wire, the value will be negative.
     */
    public int getCurrentWirePower() {
        return this.currentWirePower;
    }

    /**
     * Sets the power of this wire node.
     */
    public void setCurrentWirePower(int currentWirePower) {
        if (!this.isWireBlock()) {
            throw new IllegalStateException("Not a wire block!");
        }

        this.currentWirePower = (byte) currentWirePower;
    }

    /**
     * Returns the position of this node in the world.
     */
    public BlockPos getPosition() {
        return this.pos;
    }

    /**
     * Returns the world this node belongs to.
     */
    private World getWorld() {
        return this.graph.getWorld();
    }

    /**
     * Returns the out-going "strong power" of this node in the specified direction. Wire blocks can never
     * provide strong power.
     */
    public int getOutgoingStrongPower(Direction dir) {
        if (this.isWireBlock()) {
            return 0;
        }

        return this.getBlockState().getStrongRedstonePower(this.getWorld(), this.getPosition(), dir);
    }

    /**
     * Returns the out-going "weak power" of this node in the specified direction. Wire blocks will always
     * return their current power state minus the power fall-off across one block.
     */
    public int getOutgoingWeakPower(Direction dir) {
        if (this.isWireBlock()) {
            return this.getOutgoingWirePower();
        }

        return this.getBlockState().getWeakRedstonePower(this.getWorld(), this.getPosition(), dir);
    }

    /**
     * Returns the outgoing power provided by a wire node.
     */
    private int getOutgoingWirePower() {
        return Math.max(0, this.getCurrentWirePower() - RedstoneLogic.WIRE_POWER_LOSS_PER_BLOCK);
    }

    /**
     * Totals the weak and strong power contributed to this node by all neighboring power sources.
     */
    public int calculateIncomingEffectivePower() {
        int power = 0;

        // We can only check in the upward direction if we are not covered
        boolean canAscend = !this.fetchAdjacentNode(Direction.UP).isFullBlock();

        // Find the incoming strong power and direct weak power of our neighbors
        for (Direction dir : RedstoneLogic.BLOCK_NEIGHBOR_ALL) {
            UpdateNode adj = this.fetchAdjacentNode(dir);

            if (adj.isFullBlock()) {
                // Find the strongly provided power from our neighboring full block
                power = Math.max(power, adj.calculateIncomingStrongPower());
            }

            // We can always be weakly powered by something directly adjacent to us
            power = Math.max(power, adj.getOutgoingWeakPower(dir));
        }

        // For each horizontal neighbor, check if we can connect to a wire which in an upward or downward direction
        for (Direction dir : RedstoneLogic.WIRE_NEIGHBORS_HORIZONTAL) {
            UpdateNode adj = this.fetchAdjacentNode(dir);

            // If no block is covering this node and the adjacent block is a full-block, we can
            // check in the upwards direction for a wire
            if (canAscend && adj.isFullBlock()) {
                power = Math.max(power, adj.fetchAdjacentNode(Direction.UP).getOutgoingWirePower());
            }

            // If the adjacent block is non-full, we can check in the downwards direction for a wire
            if (!adj.isFullBlock()) {
                power = Math.max(power, adj.fetchAdjacentNode(Direction.DOWN).getOutgoingWirePower());
            }
        }

        return Math.min(power, RedstoneLogic.WIRE_MAX_POWER);
    }

    private int calculateIncomingStrongPower() {
        int power = 0;

        for (Direction dir : RedstoneLogic.BLOCK_NEIGHBOR_ALL) {
            power = Math.max(power, this.fetchAdjacentNode(dir).getOutgoingStrongPower(dir));
        }

        return power;
    }

    /**
     * Updates the node to match the real-world state of the block at the node's position. This should always be
     * called whenever the properties of this node change in the real world.
     */
    public void invalidateCache() {
        this.state = this.getWorld().getBlockState(this.pos);

        if (this.state.getBlock() == Blocks.REDSTONE_WIRE) {
            this.type = UpdateNodeBlockType.WIRE;
            this.currentWirePower = this.state.get(RedstoneWireBlock.POWER).byteValue();
        } else if (this.state.isSimpleFullBlock(this.getWorld(), this.getPosition())) {
            this.type = UpdateNodeBlockType.FULL_BLOCK;
            this.currentWirePower = 0;
        } else {
            this.type = UpdateNodeBlockType.NON_FULL_BLOCK;
            this.currentWirePower = 0;
        }
    }

    /**
     * Flushes the changes of this node to the real world. In other words, the power of this wire in the real world will
     * be updated to match the node's current power.
     */
    public void updateWireState() {
        this.getWorld().setBlockState(this.getPosition(), this.state = this.createUpdatedWireState(), 2);
    }

    public BlockState createUpdatedWireState() {
        if (!this.isWireBlock()) {
            throw new IllegalStateException("Not a wire block");
        }

        return this.getBlockState().with(RedstoneWireBlock.POWER, this.getCurrentWirePower());
    }

    public int getTraversalFlag() {
        return this.traversalFlag;
    }

    public void setTraversalFlag(int flag) {
        this.traversalFlag = (byte) flag;
    }

    public int getDarkeningThreshold() {
        return this.darkeningThreshold;
    }

    public void setDarkeningThreshold(int power) {
        this.darkeningThreshold = (byte) power;
    }

    @Override
    public String toString() {
        return String.format("UpdateNode{pos=%s, currentWirePower=%s, darkeningThreshold=%s}", this.pos, this.currentWirePower, this.darkeningThreshold);
    }
}
