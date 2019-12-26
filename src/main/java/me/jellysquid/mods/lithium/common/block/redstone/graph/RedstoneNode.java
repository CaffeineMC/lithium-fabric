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

    private final boolean wire;
    private final boolean providesStrongPower;
    private boolean modified = false;

    private final byte[] outgoingPower = new byte[6];
    private byte wirePower;

    RedstoneNode(RedstoneGraph graph, BlockPos pos) {
        this.graph = graph;
        this.pos = pos;

        this.state = this.getWorld().getBlockState(this.pos);

        this.wire = this.state.getBlock() == Blocks.REDSTONE_WIRE;
        this.wirePower = this.wire ? this.getBlockState().get(RedstoneWireBlock.POWER).byteValue() : 0;

        // Wire can never provide outgoing power
        Arrays.fill(this.outgoingPower, (byte) (this.wire ? 0 : -1));

        this.providesStrongPower = this.state.isSimpleFullBlock(this.getWorld(), this.getPosition());
    }

    public RedstoneNode getAdjacentNode(Direction direction) {
        int i = direction.ordinal();

        if (this.adjacentNodes[i] == null) {
            return this.adjacentNodes[i] = this.graph.getNodeByPosition(this.pos.offset(direction));
        }

        return this.adjacentNodes[i];
    }

    public boolean isWireBlock() {
        return this.wire;
    }

    public BlockState getBlockState() {
        return this.state;
    }

    public boolean doesBlockProvideStrongPower() {
        return this.providesStrongPower;
    }

    public int getWirePower() {
        return this.wirePower;
    }

    public void setWirePower(int power) {
        this.wirePower = (byte) power;
        this.modified = true;
    }

    public BlockPos getPosition() {
        return this.pos;
    }

    public boolean isModified() {
        return this.modified;
    }

    public int getOutgoingStrongPower(Direction dir) {
        int i = dir.ordinal();

        if (this.outgoingPower[i] == -1) {
            this.outgoingPower[i] = (byte) this.state.getStrongRedstonePower(this.getWorld(), this.getPosition(), dir);
        }

        return this.outgoingPower[i];
    }

    public int getOutgoingWeakPower(Direction dir) {
        int i = dir.ordinal();

        if (this.outgoingPower[i] == -1) {
            this.outgoingPower[i] = (byte) this.state.getWeakRedstonePower(this.getWorld(), this.getPosition(), dir);
        }

        return this.outgoingPower[i];
    }

    public int getOutgoingStrongPower() {
        int i = 0;

        for (Direction dir : RedstoneLogic.AFFECTED_NEIGHBORS) {
            i = Math.max(i, this.getAdjacentNode(dir).getOutgoingStrongPower(dir));

            if (i >= 15) {
                break;
            }
        }

        return i;
    }

    public int getOutgoingPower(Direction dir) {
        return this.doesBlockProvideStrongPower() ? this.getOutgoingStrongPower() : this.getOutgoingWeakPower(dir);
    }

    private World getWorld() {
        return this.graph.getWorld();
    }
}
