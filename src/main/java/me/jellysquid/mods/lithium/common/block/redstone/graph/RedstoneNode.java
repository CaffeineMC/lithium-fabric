package me.jellysquid.mods.lithium.common.block.redstone.graph;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

import java.util.Arrays;

public class RedstoneNode {
    private final RedstoneGraph graph;
    private final BlockPos pos;
    private final BlockView view;
    private final BlockState state;

    private final RedstoneNode[] adj = new RedstoneNode[6];

    private final boolean wire;
    private final boolean blocking;

    private final byte[] incomingPower = new byte[6];
    private byte wirePower;

    private boolean modified = false;

    RedstoneNode(RedstoneGraph graph, BlockView view, BlockPos pos) {
        this.graph = graph;
        this.pos = pos.toImmutable();
        this.view = view;

        this.state = this.view.getBlockState(this.pos);
        this.wire = this.state.getBlock() == Blocks.REDSTONE_WIRE;
        this.blocking = this.state.isSimpleFullBlock(this.view, this.pos);
        this.wirePower = this.wire ? this.getBlockState().get(RedstoneWireBlock.POWER).byteValue() : 0;

        Arrays.fill(this.incomingPower, (byte) (this.wire ? 0 : -1));
    }

    public RedstoneNode getAdjacentNode(Direction direction) {
        int i = direction.ordinal();

        if (this.adj[i] == null) {
            this.adj[i] = this.graph.getNode(this.view, this.pos.offset(direction));
        }

        return this.adj[i];
    }

    public boolean isWireBlock() {
        return this.wire;
    }

    public BlockState getBlockState() {
        return this.state;
    }

    public boolean canBlockPower() {
        return this.blocking;
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

    public int getIncomingPower(Direction dir) {
        int i = dir.ordinal();

        if (this.incomingPower[i] == -1) {
            return this.incomingPower[i] = (byte) this.state.getStrongRedstonePower(this.view, this.pos, dir);
        }

        return this.incomingPower[i];
    }
}
