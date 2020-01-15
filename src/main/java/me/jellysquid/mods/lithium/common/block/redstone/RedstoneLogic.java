package me.jellysquid.mods.lithium.common.block.redstone;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

import java.util.LinkedHashSet;

public class RedstoneLogic {
    public static final int WIRE_MIN_POWER = 0;
    public static final int WIRE_MAX_POWER = 15;
    public static final int WIRE_POWER_LOSS_PER_BLOCK = 1;

    public static final Direction[] BLOCK_NEIGHBOR_ALL = Direction.values();

    public static final Direction[] BLOCK_NEIGHBOR_UPDATE_ORDER = new Direction[]{Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH};

    public static final Direction[] WIRE_NEIGHBORS_HORIZONTAL = new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    public static final Vec3i[] WIRE_NEIGHBOR_UPDATE_ORDER = initWireUpdateOrder();

    private static Vec3i[] initWireUpdateOrder() {
        LinkedHashSet<Vec3i> set = new LinkedHashSet<>();

        for (Direction dir : BLOCK_NEIGHBOR_UPDATE_ORDER) {
            addNeighbors(set, BlockPos.ORIGIN.offset(dir));
        }

        set.remove(BlockPos.ORIGIN);

        return set.toArray(new Vec3i[0]);
    }

    private static void addNeighbors(LinkedHashSet<Vec3i> set, BlockPos pos) {
        set.add(pos);

        for (Direction dir : BLOCK_NEIGHBOR_UPDATE_ORDER) {
            set.add(pos.offset(dir));
        }
    }
}
