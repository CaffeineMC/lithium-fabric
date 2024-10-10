package net.caffeinemc.mods.lithium.mixin.block.redstone_wire;

import net.caffeinemc.mods.lithium.common.util.DirectionConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Optimizing redstone dust is tricky, but even more so if you wish to preserve behavior
 * perfectly. There are two big reasons redstone wire is laggy:
 * <br>
 * - It updates recursively. Each wire updates its power level in isolation, rather than
 * in the context of the network it is a part of. This means each wire in a network
 * could check and update its power level over half a dozen times. This also leads to
 * way more shape and block updates than necessary.
 * <br>
 * - It emits copious amounts of duplicate and redundant shape and block updates. While
 * the recursive updates are largely to blame, even a single state change leads to 18
 * redundant block updates and up to 16 redundant shape updates.
 *
 * <p>
 * Unfortunately fixing either of these aspects can be detected in-game, even if it is
 * through obscure mechanics. Removing redundant block updates can be detected with
 * something as simple as a redstone wire on a trapdoor, while removing the recursive
 * updates can be detected with locational setups that rely on a specific block update
 * order.
 *
 * <p>
 * What we can optimize, however, are the power calculations. In vanilla, these are split
 * into two parts:
 * <br>
 * - Power from non-wire components.
 * <br>
 * - Power from other redstone wires.
 * <br>
 * We can combine the two to reduce calls to World.getBlockState and BlockState.isSolidBlock
 * as well as calls to BlockState.getWeakRedstonePower and BlockState.getStrongRedstonePower.
 * We can avoid calling those last two methods on redstone wires altogether, since we know
 * they should return 0.
 * <br>
 * These changes can lead to a mspt reduction of up to 30% on top of Lithium's other
 * performance improvements.
 *
 * @author Space Walker
 */
@Mixin(RedStoneWireBlock.class)
public class RedStoneWireBlockMixin extends Block {

    private static final int MIN = 0;            // smallest possible power value
    private static final int MAX = 15;           // largest possible power value
    private static final int MAX_WIRE = MAX - 1; // largest possible power a wire can receive from another wire

    public RedStoneWireBlockMixin(Properties settings) {
        super(settings);
    }

    @Inject(
            method = "calculateTargetStrength(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)I",
            cancellable = true,
            at = @At(
                    value = "HEAD"
            )
    )
    private void getReceivedPowerFaster(Level world, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(this.getReceivedPower(world, pos));
    }

    /**
     * Calculate the redstone power a wire at the given location receives from the
     * blocks around it.
     */
    private int getReceivedPower(Level world, BlockPos pos) {
        LevelChunk chunk = world.getChunkAt(pos);
        int power = MIN;

        for (Direction dir : DirectionConstants.VERTICAL) {
            BlockPos side = pos.relative(dir);
            BlockState neighbor = chunk.getBlockState(side);

            // Wires do not accept power from other wires directly above or below them,
            // so those can be ignored. Similarly, if there is air directly above or
            // below a wire, it does not receive any power from that direction.
            if (!neighbor.isAir() && !neighbor.is(this)) {
                power = Math.max(power, this.getPowerFromVertical(world, side, neighbor, dir));

                if (power >= MAX) {
                    return MAX;
                }
            }
        }

        // In vanilla this check is done up to 4 times.
        BlockPos up = pos.above();
        boolean checkWiresAbove = !chunk.getBlockState(up).isRedstoneConductor(world, up);

        for (Direction dir : DirectionConstants.HORIZONTAL) {
            power = Math.max(power, this.getPowerFromSide(world, pos.relative(dir), dir, checkWiresAbove));

            if (power >= MAX) {
                return MAX;
            }
        }

        return power;
    }

    /**
     * Calculate the redstone power a wire receives from a block above or below it.
     * We do these positions separately because there are no wire connections
     * vertically. This simplifies the calculations a little.
     */
    private int getPowerFromVertical(Level world, BlockPos pos, BlockState state, Direction toDir) {
        int power = state.getSignal(world, pos, toDir);

        if (power >= MAX) {
            return MAX;
        }

        if (state.isRedstoneConductor(world, pos)) {
            return Math.max(power, this.getStrongPowerTo(world, pos, toDir.getOpposite()));
        }

        return power;
    }

    /**
     * Calculate the redstone power a wire receives from blocks next to it.
     */
    private int getPowerFromSide(Level world, BlockPos pos, Direction toDir, boolean checkWiresAbove) {
        LevelChunk chunk = world.getChunkAt(pos);
        BlockState state = chunk.getBlockState(pos);

        if (state.is(this)) {
            return state.getValue(BlockStateProperties.POWER) - 1;
        }

        int power = state.getSignal(world, pos, toDir);

        if (power >= MAX) {
            return MAX;
        }

        if (state.isRedstoneConductor(world, pos)) {
            power = Math.max(power, this.getStrongPowerTo(world, pos, toDir.getOpposite()));

            if (power >= MAX) {
                return MAX;
            }

            if (checkWiresAbove && power < MAX_WIRE) {
                BlockPos up = pos.above();
                BlockState aboveState = chunk.getBlockState(up);

                if (aboveState.is(this)) {
                    power = Math.max(power, aboveState.getValue(BlockStateProperties.POWER) - 1);
                }
            }
        } else if (power < MAX_WIRE) {
            BlockPos down = pos.below();
            BlockState belowState = chunk.getBlockState(down);

            if (belowState.is(this)) {
                power = Math.max(power, belowState.getValue(BlockStateProperties.POWER) - 1);
            }
        }

        return power;
    }

    /**
     * Calculate the strong power a block receives from the blocks around it.
     */
    private int getStrongPowerTo(Level world, BlockPos pos, Direction ignore) {
        int power = MIN;

        for (Direction dir : DirectionConstants.ALL) {
            if (dir != ignore) {
                BlockPos side = pos.relative(dir);
                BlockState neighbor = world.getBlockState(side);

                if (!neighbor.isAir() && !neighbor.is(this)) {
                    power = Math.max(power, neighbor.getDirectSignal(world, side, dir));

                    if (power >= MAX) {
                        return MAX;
                    }
                }
            }
        }

        return power;
    }
}
