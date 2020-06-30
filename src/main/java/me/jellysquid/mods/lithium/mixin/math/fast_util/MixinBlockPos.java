package me.jellysquid.mods.lithium.mixin.math.fast_util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

/**
 * This mixin patch speeds up {@link BlockPos} offset functions eliminating unnecessary overhead when
 * calculating a new vector position. {@link BlockPos} is often used in many parts of the game code,
 * but this patch is most noticeable during feature/structure generation.
 *
 * @author Maity
 */
@Mixin(BlockPos.class)
public abstract class MixinBlockPos extends Vec3i {
    public MixinBlockPos(int x, int y, int z) { // Only for compilation
        super(x, y, z);
    }

    /**
     * @reason Better inlining & reduce bytecode instructions
     * @author Maity
     */
    @Overwrite
    public static long asLong(int x, int y, int z) {
        return (((long) x & (long) 67108863) << 38) | (((long) y & (long) 4095)) | (((long) z & (long) 67108863) << 12);
    }

    /**
     * @reason Avoid overhead offset calculations
     * @author Maity
     */
    @Overwrite
    public BlockPos up() {
        return new BlockPos(this.getX(), this.getY() + 1, this.getZ());
    }

    /**
     * @reason Avoid overhead offset calculations
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
     * @reason Avoid overhead offset calculations
     * @author Maity
     */
    @Overwrite
    public BlockPos down() {
        return new BlockPos(this.getX(), this.getY() - 1, this.getZ());
    }

    /**
     * @reason Avoid overhead offset calculations
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
     * @reason Avoid overhead offset calculations
     * @author Maity
     */
    @Overwrite
    public BlockPos north() {
        return new BlockPos(this.getX(), this.getY(), this.getZ() - 1);
    }

    /**
     * @reason Avoid overhead offset calculations
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
     * @reason Avoid overhead offset calculations
     * @author Maity
     */
    @Overwrite
    public BlockPos south() {
        return new BlockPos(this.getX(), this.getY(), this.getZ() + 1);
    }

    /**
     * @reason Avoid overhead offset calculations
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
     * @reason Avoid overhead offset calculations
     * @author Maity
     */
    @Overwrite
    public BlockPos west() {
        return new BlockPos(this.getX() - 1, this.getY(), this.getZ());
    }

    /**
     * @reason Avoid overhead offset calculations
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
     * @reason Avoid overhead offset calculations
     * @author Maity
     */
    @Overwrite
    public BlockPos east() {
        return new BlockPos(this.getX() + 1, this.getY(), this.getZ());
    }

    /**
     * @reason Avoid overhead offset calculations
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
     * @reason Avoid overhead offset calculations
     * @author Maity
     */
    @Overwrite
    public BlockPos offset(Direction direction) {
        switch (direction) {
            case UP:
                return new BlockPos(this.getX(), this.getY() + 1, this.getZ());
            case DOWN:
                return new BlockPos(this.getX(), this.getY() - 1, this.getZ());
            case NORTH:
                return new BlockPos(this.getX(), this.getY(), this.getZ() - 1);
            case SOUTH:
                return new BlockPos(this.getX(), this.getY(), this.getZ() + 1);
            case WEST:
                return new BlockPos(this.getX() - 1, this.getY(), this.getZ());
            case EAST:
                return new BlockPos(this.getX() + 1, this.getY(), this.getZ());
            default:
                // [VanillaCopy]
                return new BlockPos(this.getX() + direction.getOffsetX(), this.getY() + direction.getOffsetY(), this.getZ() + direction.getOffsetZ());
        }
    }

    /**
     * @reason Avoid overhead offset calculations
     * @author Maity
     */
    @Overwrite
    public BlockPos offset(Direction direction, int distance) {
        if (distance == 0) {
            return (BlockPos) (Object) this;
        }

        switch (direction) {
            case UP:
                return new BlockPos(this.getX(), this.getY() + distance, this.getZ());
            case DOWN:
                return new BlockPos(this.getX(), this.getY() - distance, this.getZ());
            case NORTH:
                return new BlockPos(this.getX(), this.getY(), this.getZ() - distance);
            case SOUTH:
                return new BlockPos(this.getX(), this.getY(), this.getZ() + distance);
            case WEST:
                return new BlockPos(this.getX() - distance, this.getY(), this.getZ());
            case EAST:
                return new BlockPos(this.getX() + distance, this.getY(), this.getZ());
            default:
                // [VanillaCopy]
                return new BlockPos(this.getX() + direction.getOffsetX() * distance, this.getY() + direction.getOffsetY() * distance, this.getZ() + direction.getOffsetZ() * distance);
        }
    }
}
