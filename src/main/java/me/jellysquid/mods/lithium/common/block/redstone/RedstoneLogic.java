package me.jellysquid.mods.lithium.common.block.redstone;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

import java.util.LinkedHashSet;

public class RedstoneLogic {
    public static final Direction[] AFFECTED_NEIGHBORS = Direction.values();

    public static final Direction[] NEIGHBOR_UPDATE_ORDER = new Direction[]{Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH};

    public static final Direction[] HORIZONTAL_ITERATION_ORDER = new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    public static final Vec3i[] WIRE_UPDATE_ORDER = initWireUpdateOrder();

    private static Vec3i[] initWireUpdateOrder() {
        LinkedHashSet<Vec3i> set = new LinkedHashSet<>();

        addNeighbors(set, BlockPos.ORIGIN);

        for (Direction dir : AFFECTED_NEIGHBORS) {
            addNeighbors(set, BlockPos.ORIGIN.offset(dir));
        }

        return set.toArray(new Vec3i[0]);
    }

    private static void addNeighbors(LinkedHashSet<Vec3i> set, BlockPos pos) {
        for (Direction dir : NEIGHBOR_UPDATE_ORDER) {
            set.add(pos.offset(dir));
        }
    }
}
