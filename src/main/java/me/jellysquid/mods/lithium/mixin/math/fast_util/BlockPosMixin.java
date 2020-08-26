package me.jellysquid.mods.lithium.mixin.math.fast_util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(BlockPos.class)
public abstract class BlockPosMixin extends Vec3i {
    static {
        assert Direction.DOWN.ordinal() == 0;
        assert Direction.UP.ordinal() == 1;
        assert Direction.NORTH.ordinal() == 2;
        assert Direction.SOUTH.ordinal() == 3;
        assert Direction.WEST.ordinal() == 4;
        assert Direction.EAST.ordinal() == 5;
        assert Direction.values().length == 6;
    }

    private BlockPosMixin(int x, int y, int z) {
        super(x, y, z);
    }

    /**
     * @reason Manually write function logic avoiding unnecessary costs
     * @author Maity
     */
    @Overwrite
    public BlockPos up() {
        return new BlockPos(this.getX(), this.getY() + 1, this.getZ());
    }

    /**
     * @reason Manually write function logic avoiding unnecessary costs
     * @author Maity
     */
    @Overwrite
    public BlockPos up(int distance) {
        if (distance == 0) { // We do not need to create a new object once again
            return (BlockPos) (Object) this;
        }

        return new BlockPos(this.getX(), this.getY() + distance, this.getZ());
    }

    /**
     * @reason Manually write function logic avoiding unnecessary costs
     * @author Maity
     */
    @Overwrite
    public BlockPos down() {
        return new BlockPos(this.getX(), this.getY() - 1, this.getZ());
    }

    /**
     * @reason Manually write function logic avoiding unnecessary costs
     * @author Maity
     */
    @Overwrite
    public BlockPos down(int distance) {
        if (distance == 0) {
            return (BlockPos) (Object) this;
        }

        return new BlockPos(this.getX(), this.getY() - distance, this.getZ());
    }

    /**
     * @reason Manually write function logic avoiding unnecessary costs
     * @author Maity
     */
    @Overwrite
    public BlockPos north() {
        return new BlockPos(this.getX(), this.getY(), this.getZ() - 1);
    }

    /**
     * @reason Manually write function logic avoiding unnecessary costs
     * @author Maity
     */
    @Overwrite
    public BlockPos north(int distance) {
        if (distance == 0) {
            return (BlockPos) (Object) this;
        }

        return new BlockPos(this.getX(), this.getY(), this.getZ() - distance);
    }

    /**
     * @reason Manually write function logic avoiding unnecessary costs
     * @author Maity
     */
    @Overwrite
    public BlockPos south() {
        return new BlockPos(this.getX(), this.getY(), this.getZ() + 1);
    }

    /**
     * @reason Manually write function logic avoiding unnecessary costs
     * @author Maity
     */
    @Overwrite
    public BlockPos south(int distance) {
        if (distance == 0) {
            return (BlockPos) (Object) this;
        }

        return new BlockPos(this.getX(), this.getY(), this.getZ() + distance);
    }

    /**
     * @reason Manually write function logic avoiding unnecessary costs
     * @author Maity
     */
    @Overwrite
    public BlockPos west() {
        return new BlockPos(this.getX() - 1, this.getY(), this.getZ());
    }

    /**
     * @reason Manually write function logic avoiding unnecessary costs
     * @author Maity
     */
    @Overwrite
    public BlockPos west(int distance) {
        if (distance == 0) {
            return (BlockPos) (Object) this;
        }

        return new BlockPos(this.getX() - distance, this.getY(), this.getZ());
    }

    /**
     * @reason Manually write function logic avoiding unnecessary costs
     * @author Maity
     */
    @Overwrite
    public BlockPos east() {
        return new BlockPos(this.getX() + 1, this.getY(), this.getZ());
    }

    /**
     * @reason Manually write function logic avoiding unnecessary costs
     * @author Maity
     */
    @Overwrite
    public BlockPos east(int distance) {
        if (distance == 0) {
            return (BlockPos) (Object) this;
        }

        return new BlockPos(this.getX() + distance, this.getY(), this.getZ());
    }

    /**
     * Direction switching is quicker than vanilla calculations.
     *
     * @reason Use better implementation
     * @author Maity
     */
    @Overwrite
    public BlockPos offset(Direction direction, int distance) {
        if (distance == 0) {
            return (BlockPos) (Object) this;
        }

        switch (direction.ordinal()) {
            case 0: // DOWN
                return new BlockPos(this.getX(), this.getY() - distance, this.getZ());
            case 1: // UP
                return new BlockPos(this.getX(), this.getY() + distance, this.getZ());
            case 2: // NORTH
                return new BlockPos(this.getX(), this.getY(), this.getZ() - distance);
            case 3: // SOUTH
                return new BlockPos(this.getX(), this.getY(), this.getZ() + distance);
            case 4: // WEST
                return new BlockPos(this.getX() - distance, this.getY(), this.getZ());
            case 5: // EAST
                return new BlockPos(this.getX() + distance, this.getY(), this.getZ());
            default:
                // [VanillaCopy]
                return new BlockPos(this.getX() + direction.getOffsetX() * distance, this.getY() + direction.getOffsetY() * distance, this.getZ() + direction.getOffsetZ() * distance);
        }
    }

    /**
     * @reason Use better implementation
     * @author Maity
     */
    @Overwrite
    public BlockPos offset(Direction direction) {
        return this.offset(direction, 1);
    }
}
