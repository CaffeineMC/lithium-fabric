package me.jellysquid.mods.lithium.common.block.redstone.graph;

import me.jellysquid.mods.lithium.common.block.redstone.RedstoneLogic;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class UpdateNode {
    private final UpdateGraph graph;
    private final BlockPos pos;

    private final UpdateNode[] adjacentNodes = new UpdateNode[6];
    private final List<UpdateNode> connections = new ArrayList<>(0);

    private BlockState state;
    private UpdateNodeBlockType type;

    private byte currentWirePower = 0;
    private byte darkeningThreshold = -1;

    private byte flags;

    UpdateNode(UpdateGraph graph, BlockPos pos) {
        this.graph = graph;
        this.pos = pos.toImmutable();

        this.invalidateWorldState();
    }

    /**
     * Returns the node directly adjacent from this node in the specified direction. This will make use of a cache
     * to make traversing the node graph extremely quick. If a node is not in the cache, it will be looked for in the
     * graph using a slower method. If the graph doesn't contain the node, it will be initialized and linked to this
     * node in the opposite direction.
     */
    public UpdateNode getAdjacent(Direction dir) {
        UpdateNode adj = this.adjacentNodes[dir.ordinal()];

        if (adj == null) {
            adj = this.graph.getOrCreateNode(this.pos.offset(dir));
            adj.adjacentNodes[dir.getOpposite().ordinal()] = this; // Link the node back to us

            this.adjacentNodes[dir.ordinal()] = adj;
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
        boolean canAscend = !this.getAdjacent(Direction.UP).isFullBlock();

        // Find the incoming strong power and direct weak power of our neighbors
        for (Direction dir : RedstoneLogic.INCOMING_POWER_DIRECTIONS) {
            UpdateNode adj = this.getAdjacent(dir);

            if (adj.isFullBlock()) {
                // Find the strongly provided power from our neighboring full block
                power = Math.max(power, adj.calculateIncomingStrongPower());
            }

            // We can always be weakly powered by something directly adjacent to us
            power = Math.max(power, adj.getOutgoingWeakPower(dir));
        }

        // For each horizontal neighbor, check if we can connect to a wire which in an upward or downward direction
        for (Direction dir : RedstoneLogic.WIRE_NEIGHBORS_HORIZONTAL) {
            UpdateNode adj = this.getAdjacent(dir);

            // If no block is covering this node and the adjacent block is a full-block, we can
            // check in the upwards direction for a wire
            if (canAscend && adj.isFullBlock()) {
                power = Math.max(power, adj.getAdjacent(Direction.UP).getOutgoingWirePower());
            }

            // If the adjacent block is non-full, we can check in the downwards direction for a wire
            if (!adj.isFullBlock()) {
                power = Math.max(power, adj.getAdjacent(Direction.DOWN).getOutgoingWirePower());
            }
        }

        return Math.min(power, RedstoneLogic.WIRE_MAX_POWER);
    }

    private int calculateIncomingStrongPower() {
        int power = 0;

        for (Direction dir : RedstoneLogic.INCOMING_POWER_DIRECTIONS) {
            power = Math.max(power, this.getAdjacent(dir).getOutgoingStrongPower(dir));
        }

        return power;
    }

    /**
     * Updates the node to match the real-world state of the block at the node's position. This should always be
     * called whenever the properties of this node change in the real world.
     */
    public void invalidateWorldState() {
        this.state = this.graph.getBlockAccess().getBlockState(this.pos);

        if (this.state.getBlock() == Blocks.REDSTONE_WIRE) {
            this.type = UpdateNodeBlockType.WIRE;
            this.currentWirePower = this.state.get(RedstoneWireBlock.POWER).byteValue();
        } else if (this.state.isSolidBlock(this.getWorld(), this.getPosition())) {
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
        this.graph.getBlockAccess().setBlockState(this.getPosition(), this.state = this.createUpdatedWireState());
    }

    public BlockState createUpdatedWireState() {
        if (!this.isWireBlock()) {
            throw new IllegalStateException("Not a wire block");
        }

        return this.getBlockState().with(RedstoneWireBlock.POWER, this.getCurrentWirePower());
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

    /**
     * Returns whether or not the block beneath this node can support a wire block above it.
     */
    public boolean isWireAtValidLocation() {
        return this.getAdjacent(Direction.DOWN).canSupportWireBlock();
    }

    /**
     * [VanillaCopy] RedstoneBlockWire#canPlaceAt
     * Returns true if the node can support a wire block above it, otherwise false.
     */
    public boolean canSupportWireBlock() {
        this.invalidateWorldState();

        WorldView world = this.getWorld();
        BlockState state = this.getBlockState();
        BlockPos pos = this.getPosition();

        return state.isSideSolidFullSquare(world, pos, Direction.UP) || state.getBlock() == Blocks.HOPPER;
    }

    /**
     * Destroys this wire node under the premise that it has been invalidated by the block beneath it changing. This
     * will call into the world to remove the wire block, which will in turn cause notification to be propagated to the
     * redstone engine. The engine then has the responsibility to handle cancellation of any currently propagating
     * signals.
     */
    public void destroyWire() {
        Block.dropStacks(this.getBlockState(), this.getWorld(), this.getPosition());

        this.getWorld().removeBlock(this.getPosition(), false);
    }

    /**
     * Invalidates all the connections this node has to other nodes. This should be called when the node is removed.
     */
    public void invalidateConnections() {
        this.connections.clear();
    }

    /**
     * Adds a connection from this node to the other node. This will only modify this node.
     */
    public void addConnection(UpdateNode node) {
        this.connections.add(node);
    }

    /**
     * Returns all outgoing connections from this node to other nodes.
     */
    public Collection<UpdateNode> getConnections() {
        return Collections.unmodifiableCollection(this.connections);
    }

    /**
     * Checks if the specified flag has been marked for this node and adds it if not.
     * @param flag The flag to check
     * @return False if the flag has already been marked on this node, otherwise true
     */
    public boolean checkAndMarkFlag(UpdateFlag flag) {
        int flags = Byte.toUnsignedInt(this.flags);
        int bit = 1 << flag.ordinal();

        if ((flags & bit) != 0) {
            return false;
        }

        this.flags = (byte) (flags | bit);

        return true;
    }

    /**
     * Resets all flags for this node to their default value.
     */
    public void clearFlags() {
        this.flags = (byte) 0;
    }

    /**
     * Notifies the block in the world belonging to this node that its neighbors have updated. Optionally, observing
     * blocks can be notified and updated.
     *
     * @param origin The neighbor causing this update
     * @param dir The direction outward from {@param origin} to this node
     * @param updateObservers If true, observing blocks directly adjacent to the node will have their state updated
     */
    public void update(UpdateNode origin, Direction dir, boolean updateObservers) {
        this.invalidateWorldState();

        BlockState state = updateObservers ? this.getUpdatedBlockState(origin, dir) : this.getBlockState();

        if (state.getBlock() != Blocks.REDSTONE_WIRE) {
            state.neighborUpdate(this.getWorld(), this.getPosition(), Blocks.REDSTONE_WIRE, origin.getPosition(), false);
        }
    }

    /**
     * Determines the new state of this node from being updated by a specific neighbor if it is an observing block.
     */
    private BlockState getUpdatedBlockState(UpdateNode origin, Direction dir) {
        BlockState state = this.getBlockState();

        if (state.getBlock() != Blocks.REDSTONE_WIRE) {
            BlockState newState = state.getStateForNeighborUpdate(dir.getOpposite(), state, this.getWorld(), this.getPosition(), origin.getPosition());
            replaceBlock(state, newState, this.getWorld(), this.getPosition());

            state = newState;
        }

        return state;
    }

    private void replaceBlock(BlockState state, BlockState updatedState, IWorld world, BlockPos pos) {
        if (updatedState == state) {
            return;
        }

        if (updatedState.isAir()) {
            if (world.isClient()) {
                return;
            }

            world.breakBlock(pos, true);
        } else {
            this.graph.getBlockAccess().setBlockState(pos, updatedState);
        }

    }
}
