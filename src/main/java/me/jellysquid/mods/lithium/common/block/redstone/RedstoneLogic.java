package me.jellysquid.mods.lithium.common.block.redstone;

import me.jellysquid.mods.lithium.common.block.redstone.graph.RedstoneNode;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

import java.util.LinkedHashSet;

public class RedstoneLogic {
    public static final int WIRE_POWER_LOSS_PER_BLOCK = 1;
    public static final int WIRE_MAX_POWER = 15;

    public static final Direction[] ALL_NEIGHBORS = Direction.values();

    public static final Direction[] REDSTONE_UPDATE_ORDER = new Direction[]{Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH};

    public static final Direction[] HORIZONTAL_NEIGHBORS = new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    public static final Vec3i[] WIRE_UPDATE_ORDER = initWireUpdateOrder();

    private static Vec3i[] initWireUpdateOrder() {
        LinkedHashSet<Vec3i> set = new LinkedHashSet<>();

        addNeighbors(set, BlockPos.ORIGIN);

        for (Direction dir : ALL_NEIGHBORS) {
            addNeighbors(set, BlockPos.ORIGIN.offset(dir));
        }

        return set.toArray(new Vec3i[0]);
    }

    private static void addNeighbors(LinkedHashSet<Vec3i> set, BlockPos pos) {
        for (Direction dir : REDSTONE_UPDATE_ORDER) {
            set.add(pos.offset(dir));
        }
    }

    public static RedstoneNode getNextWireInDirection(RedstoneNode node, Direction dir, boolean isNodeCovered) {
        RedstoneNode adj = node.getAdjacentNode(dir);

        if (adj.isWireBlock()) {
            return adj;
        }

        if (adj.isFullBlock()) {
            if (!isNodeCovered) {
                RedstoneNode aboveAdj = adj.getAdjacentNode(Direction.UP);

                if (aboveAdj.isWireBlock()) {
                    return aboveAdj;
                }
            }
        } else {
            RedstoneNode aboveAdj = adj.getAdjacentNode(Direction.UP);

            if (aboveAdj.isWireBlock()) {
                return aboveAdj;
            }

            RedstoneNode belowAdj = adj.getAdjacentNode(Direction.DOWN);

            if (belowAdj.isWireBlock()) {
                return belowAdj;
            }
        }

        return null;
    }

    public static int getEmittedPowerInDirection(RedstoneNode node, Direction facing, boolean isNodeCovered) {
        RedstoneNode adj = node.getAdjacentNode(facing);

        int power = adj.getProvidedWeakPower(facing);

        if (adj.isFullBlock()) {
            power = RedstoneLogic.addStrongPower(adj, power);

            if (!isNodeCovered) {
                power = RedstoneLogic.addWeakWirePower(adj, Direction.UP, power);
            }
        } else {
            power = RedstoneLogic.addWeakWirePower(adj, Direction.DOWN, power);
        }

        return power;
    }

    private static int addStrongPower(RedstoneNode node, int power) {
        for (Direction dir : RedstoneLogic.ALL_NEIGHBORS) {
            power = Math.max(power, node.getAdjacentNode(dir).getProvidedStrongPower(dir));
        }

        return power;
    }

    private static int addWeakWirePower(RedstoneNode node, Direction dir, int power) {
        return Math.max(power, node.getAdjacentNode(dir).getWirePower() - 1);
    }
}
