package me.jellysquid.mods.lithium.common.block.redstone.graph;

import me.jellysquid.mods.lithium.common.block.redstone.RedstoneLogic;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Arrays;

public class RedstoneNode {
    private final RedstoneGraph graph;
    private final BlockPos pos;
    private final BlockState state;

    private final RedstoneNode[] adjacentNodes = new RedstoneNode[6];

    private final boolean isWire, isFullBlock;

    public int wirePower, prevWirePower;
    public int traversalFlag;

    public byte[] strongPower = new byte[6];
    public byte[] weakPower = new byte[6];

    RedstoneNode(RedstoneGraph graph, BlockPos pos) {
        this.graph = graph;
        this.pos = pos;

        this.state = this.getWorld().getBlockState(this.pos);

        this.isWire = this.state.getBlock() == Blocks.REDSTONE_WIRE;
        this.isFullBlock = this.state.isSimpleFullBlock(this.getWorld(), this.getPosition());

        this.wirePower = this.isWire ? this.state.get(RedstoneWireBlock.POWER) : 0;

        Arrays.fill(this.strongPower, (byte) -1);
        Arrays.fill(this.weakPower, (byte) -1);
    }

    public RedstoneNode getAdjacentNode(Direction direction) {
        int i = direction.ordinal();

        if (this.adjacentNodes[i] == null) {
            return this.adjacentNodes[i] = this.graph.getOrCreateNode(this.pos.offset(direction));
        }

        return this.adjacentNodes[i];
    }

    public boolean isWireBlock() {
        return this.isWire;
    }

    public BlockState getBlockState() {
        return this.state;
    }

    public int getWirePower() {
        return this.wirePower;
    }

    public void setWirePower(int wirePower) {
        this.wirePower = wirePower;
    }

    public BlockPos getPosition() {
        return this.pos;
    }

    private World getWorld() {
        return this.graph.getWorld();
    }

    public boolean isFullBlock() {
        return this.isFullBlock;
    }

    public int getPreviousWirePower() {
        return this.prevWirePower;
    }

    public void setPreviousWirePower(int power) {
        this.prevWirePower = power;
    }

    public int getProvidedStrongPower(Direction dir) {
        if (this.isWireBlock()) {
            return 0;
        }

        int i = dir.ordinal();

        if (this.strongPower[i] >= 0) {
            return this.strongPower[i];
        }

        return this.strongPower[i] = (byte) this.getBlockState().getStrongRedstonePower(this.getWorld(), this.getPosition(), dir);
    }

    public int getProvidedWeakPower(Direction dir) {
        if (this.isWireBlock()) {
            return this.getWirePower() - RedstoneLogic.WIRE_POWER_LOSS_PER_BLOCK;
        }

        int i = dir.ordinal();

        if (this.weakPower[i] >= 0) {
            return this.weakPower[i];
        }

        return this.weakPower[i] = (byte) this.getBlockState().getWeakRedstonePower(this.getWorld(), this.getPosition(), dir);
    }

    public int getPowerFromNeighbors() {
        boolean covered = this.getAdjacentNode(Direction.UP).isFullBlock();

        int power = 0;

        for (Direction dir : RedstoneLogic.ALL_NEIGHBORS) {
            power = Math.max(power, RedstoneLogic.getEmittedPowerInDirection(this, dir, covered));
        }

        return Math.min(power, RedstoneLogic.WIRE_MAX_POWER);
    }

}
