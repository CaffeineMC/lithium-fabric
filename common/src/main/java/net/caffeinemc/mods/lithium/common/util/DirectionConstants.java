package net.caffeinemc.mods.lithium.common.util;

import net.minecraft.core.Direction;

/**
 * Pre-initialized constants to avoid unnecessary allocations.
 */
public final class DirectionConstants {
    private DirectionConstants() {}

    public static final Direction[] ALL = Direction.values();
    public static final Direction[] VERTICAL = {Direction.DOWN, Direction.UP};
    public static final Direction[] HORIZONTAL = {Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH};
    public static final byte[] HORIZONTAL_OPPOSITE_INDICES = {1, 0, 3, 2};
}
