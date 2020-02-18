package me.jellysquid.mods.lithium.common.block.redstone;

import net.minecraft.util.math.Direction;

public class RedstoneLogic {
    /**
     * The minimum power state of a Redstone wire.
     **/
    public static final int WIRE_MIN_POWER = 0;

    /**
     * The maximum power state of a Redstone wire.
     **/
    public static final int WIRE_MAX_POWER = 15;

    /**
     * The amount of power which is loss through a wire across one block.
     **/
    public static final int WIRE_POWER_LOSS_PER_BLOCK = 1;

    /**
     * All directions which can provide power to a Redstone wire.
     **/
    public static final Direction[] INCOMING_POWER_DIRECTIONS = Direction.values();

    /**
     * The order of directions in which block updates should be made when a wire state changes.
     **/
    public static final Direction[] BLOCK_NEIGHBOR_UPDATE_ORDER = new Direction[]{Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH};

    /**
     * The horizontal directions which wire can connect in.
     **/
    public static final Direction[] WIRE_NEIGHBORS_HORIZONTAL = new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
}
