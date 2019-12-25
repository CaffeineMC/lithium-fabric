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
    private final RedstoneGraph graph = new RedstoneGraph();

    private List<PendingUpdate> pendingUpdates = new ArrayList<>();

    private Stack<PendingUpdate> pendingWireUpdates = new Stack<>();

    private final World world;

    private boolean isUpdating;

    public RedstoneEngine(World world) {
        this.world = world;
    }

    public int getReceivedRedstonePower(BlockPos pos) {
        int power = 0;

        RedstoneNode info = this.graph.getNode(this.world, pos);

        for (Direction dir : RedstoneLogic.AFFECTED_NEIGHBORS) {
            int adjPower = this.getEmittedRedstonePower(info.getAdjacentNode(dir), dir);

            if (adjPower > power) {
                power = adjPower;
            }

            if (power >= 15) {
                break;
            }
        }

        return power;
    }

    public int getEmittedRedstonePower(RedstoneNode info, Direction direction) {
        int power;

        if (info.canBlockPower()) {
            power = this.getIncomingDirectPower(info);
        } else {
            power = info.isWireBlock() ? 0 : info.getBlockState().getWeakRedstonePower(this.world, info.getPosition(), direction);
        }

        return power;
    }

    public int getIncomingDirectPower(RedstoneNode info) {
        int i = 0;

        for (Direction dir : RedstoneLogic.AFFECTED_NEIGHBORS) {
            i = Math.max(i, info.getAdjacentNode(dir).getIncomingPower(dir));

            if (i >= 15) {
                break;
            }
        }

        return i;
    }

    public void enqueueNeighbor(BlockPos updatingPos, Block originBlock, BlockPos originPos) {
        BlockState updatingState = this.graph.getNode(this.world, updatingPos).getBlockState();

        if (updatingState.getBlock() != originBlock) {
            this.pendingUpdates.add(new PendingUpdate(updatingPos, updatingState, originBlock, originPos));
        } else {
            this.pendingWireUpdates.add(new PendingUpdate(updatingPos, updatingState, originBlock, originPos));
        }
    }

    public void setWireStrength(BlockPos pos, int power) {
        this.graph.getNode(this.world, pos).setWirePower(power);
    }

    public int getPower(BlockPos pos, BlockState state) {
        RedstoneNode info = this.graph.getNode(this.world, pos);

        if (info.isWireBlock()) {
            return info.getWirePower();
        }

        return state.get(RedstoneWireBlock.POWER);
    }

    public int increasePower(int power, RedstoneNode node) {
        if (!node.isWireBlock()) {
            return power;
        }

        return Math.max(node.getWirePower(), power);
    }

    public void finish() {
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
        RedstoneNode aboveNode = this.graph.getNode(this.world, pos.up());

        int powerContributed = 0;

        for (Direction dir : RedstoneLogic.HORIZONTAL_ITERATION_ORDER) {
            BlockPos adjPos = pos.offset(dir);
            RedstoneNode adjNode = this.graph.getNode(this.world, adjPos);

            powerContributed = this.increasePower(powerContributed, adjNode);

            if (adjNode.canBlockPower()) {
                if (!aboveNode.canBlockPower()) {
                    powerContributed = this.increasePower(powerContributed, this.graph.getNode(this.world, adjPos.up()));
                }
            } else {
                powerContributed = this.increasePower(powerContributed, this.graph.getNode(this.world, adjPos.down()));
            }
        }

        return powerContributed;
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
