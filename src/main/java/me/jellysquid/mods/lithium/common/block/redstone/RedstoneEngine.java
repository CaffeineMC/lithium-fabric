package me.jellysquid.mods.lithium.common.block.redstone;

import me.jellysquid.mods.lithium.common.block.redstone.graph.RedstoneGraph;
import me.jellysquid.mods.lithium.common.block.redstone.graph.RedstoneNode;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class RedstoneEngine {
    private final RedstoneGraph graph;

    private List<PendingUpdate> pendingUpdates = new ArrayList<>();

    private Stack<PendingUpdate> pendingWireUpdates = new Stack<>();

    private final World world;

    private boolean isUpdating;

    public RedstoneEngine(World world) {
        this.world = world;
        this.graph = new RedstoneGraph(world);
    }

    public int getReceivedPower(BlockPos pos) {
        int power = 0;

        RedstoneNode info = this.graph.getNodeByPosition(pos);

        for (Direction dir : RedstoneLogic.AFFECTED_NEIGHBORS) {
            int adjPower = info.getAdjacentNode(dir).getOutgoingPower(dir);

            if (adjPower > power) {
                power = adjPower;
            }

            if (power >= 15) {
                break;
            }
        }

        return power;
    }

    public void enqueueNeighbor(BlockPos updatingPos, Block originBlock, BlockPos originPos) {
        BlockState updatingState = this.graph.getNodeByPosition(updatingPos).getBlockState();

        if (updatingState.getBlock() != originBlock) {
            this.pendingUpdates.add(new PendingUpdate(updatingPos, updatingState, originBlock, originPos));
        } else {
            this.pendingWireUpdates.add(new PendingUpdate(updatingPos, updatingState, originBlock, originPos));
        }
    }

    public void setWireCurrentPower(BlockPos pos, int power) {
        this.graph.getNodeByPosition(pos).setWirePower(power);
    }

    public int getWireCurrentPower(BlockPos pos, BlockState state) {
        RedstoneNode info = this.graph.getNodeByPosition(pos);

        if (info.isWireBlock()) {
            return info.getWirePower();
        }

        return state.get(RedstoneWireBlock.POWER);
    }

    public void flush() {
        if (this.isUpdating) {
            throw new IllegalStateException("Already updating!");
        }

        this.isUpdating = true;

        try {
            this.processWireUpdates();
            this.processWireStateChanges();
            this.processOtherUpdates();
        } finally {
            this.isUpdating = false;
        }
    }

    private void processWireUpdates() {
        while (!this.pendingWireUpdates.isEmpty()) {
            PendingUpdate update = this.pendingWireUpdates.pop();
            update.updatingState.neighborUpdate(this.world, update.updatingPos, update.originBlock, update.originPos, false);
        }
    }

    private void processWireStateChanges() {
        for (RedstoneNode info : this.graph) {
            if (!info.isModified()) {
                continue;
            }

            this.world.setBlockState(info.getPosition(), info.getBlockState().with(RedstoneWireBlock.POWER, info.getWirePower()), 2);
        }

        this.graph.clear();
    }

    private void processOtherUpdates() {
        List<PendingUpdate> updates = this.pendingUpdates;

        this.pendingUpdates = new ArrayList<>();

        for (PendingUpdate update : updates) {
            update.updatingState.neighborUpdate(this.world, update.updatingPos, update.originBlock, update.originPos, false);
        }
    }

    public int getPowerContributed(BlockPos pos) {
        BlockPos.Mutable mut = new BlockPos.Mutable();

        RedstoneNode aboveNode = this.graph.getNodeByPosition(mut.set(pos.getX(), pos.getY() + 1, pos.getZ()));

        int power = 0;

        for (Direction dir : RedstoneLogic.HORIZONTAL_ITERATION_ORDER) {
            mut.set(pos.getX() + dir.getOffsetX(), pos.getY() + dir.getOffsetY(), pos.getZ() + dir.getOffsetZ());

            RedstoneNode adjNode = this.graph.getNodeByPosition(mut);

            power = this.mergePowerLevel(power, adjNode);

            if (adjNode.doesBlockProvideStrongPower()) {
                if (!aboveNode.doesBlockProvideStrongPower()) {
                    power = this.mergePowerLevel(power, this.graph.getNodeByPosition(mut.setOffset(Direction.UP)));
                }
            } else {
                power = this.mergePowerLevel(power, this.graph.getNodeByPosition(mut.setOffset(Direction.DOWN)));
            }
        }

        return power;
    }

    private int mergePowerLevel(int power, RedstoneNode node) {
        if (!node.isWireBlock()) {
            return power;
        }

        return Math.max(node.getWirePower(), power);
    }

    public boolean isUpdating() {
        return this.isUpdating;
    }

    private static class PendingUpdate {
        final BlockPos updatingPos;
        final BlockState updatingState;
        final Block originBlock;
        final BlockPos originPos;

        private PendingUpdate(BlockPos updatingPos, BlockState updatingState, Block originBlock, BlockPos originPos) {
            this.updatingPos = updatingPos;
            this.updatingState = updatingState;
            this.originBlock = originBlock;
            this.originPos = originPos;
        }
    }

}
