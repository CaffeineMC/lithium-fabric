package me.jellysquid.mods.lithium.mixin.block.redstone_wire;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * @author Space Walker
 */
@Mixin(RedstoneWireBlock.class)
public class RedstoneWireBlockMixin extends Block {

    private static final Direction[] DIRECTIONS = Direction.values();
    private static final Direction[] DIRECTIONS_VIRTICAL = { Direction.DOWN, Direction.UP };
    private static final Direction[] DIRECTIONS_HORIZONTAL = { Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH };

    private static final int MIN = 0;
    private static final int MAX = 15;
    private static final int MAX_WIRE = MAX - 1;

    public RedstoneWireBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(
            method = "getReceivedRedstonePower",
            cancellable = true,
            at = @At(
                    value = "HEAD"
            )
    )
    private void getReceivedPowerFaster(World world, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(this.getReceivedPower(world, pos));
        cir.cancel();
    }

    private int getReceivedPower(World world, BlockPos pos) {
        int power = MIN;
        
        for (Direction dir : DIRECTIONS_VIRTICAL) {
            power = Math.max(power, this.getPowerFromVertical(world, pos.offset(dir), dir));
            
            if (power >= MAX) {
                return MAX;
            }
        }
        
        BlockPos up = pos.up();
        boolean checkWiresAbove = world.getBlockState(up).isSolidBlock(world, up);
        
        for (Direction dir : DIRECTIONS_HORIZONTAL) {
            power = Math.max(power, this.getPowerFromSide(world, pos.offset(dir), dir, checkWiresAbove));
            
            if (power >= MAX) {
                return MAX;
            }
        }
        
        return power;
    }

    private int getPowerFromVertical(World world, BlockPos pos, Direction toDir) {
        BlockState state = world.getBlockState(pos);
        
        if (state.isOf(this) || state.isAir()) {
            return MIN;
        }
        
        int power = state.getWeakRedstonePower(world, pos, toDir);
        
        if (power >= MAX) {
            return MAX;
        }
        
        if (state.isSolidBlock(world, pos)) {
            return Math.max(power, this.getStrongPowerTo(world, pos, toDir));
        }
        
        return power;
    }

    private int getPowerFromSide(World world, BlockPos pos, Direction toDir, boolean checkWiresAbove) {
        BlockState state = world.getBlockState(pos);
        
        if (state.isOf(this)) {
            return state.get(Properties.POWER) - 1;
        }
        
        int power = state.getWeakRedstonePower(world, pos, toDir);
        
        if (power >= MAX) {
            return MAX;
        }
        
        if (state.isSolidBlock(world, pos)) {
            power = Math.max(power, this.getStrongPowerTo(world, pos, toDir));
            
            if (power >= MAX) {
                return MAX;
            }
            
            if (checkWiresAbove && power < MAX_WIRE) {
                BlockPos up = pos.up();
                BlockState aboveState = world.getBlockState(up);
                
                if (aboveState.isOf(this)) {
                    power = Math.max(power, aboveState.get(Properties.POWER) - 1);
                }
            }
        } else if (power < MAX_WIRE) {
            BlockPos down = pos.down();
            BlockState aboveState = world.getBlockState(down);
            
            if (aboveState.isOf(this)) {
                power = Math.max(power, aboveState.get(Properties.POWER) - 1);
            }
        }
        
        return power;
    }

    private int getStrongPowerTo(World world, BlockPos pos, Direction ignore) {
        int power = MIN;
        
        for (Direction dir : DIRECTIONS) {
            if (dir != ignore) {
                BlockPos side = pos.offset(dir);
                BlockState neighbor = world.getBlockState(side);
                
                if (!neighbor.isOf(this) && !neighbor.isAir()) {
                    power = Math.max(power, neighbor.getStrongRedstonePower(world, side, dir));
                    
                    if (power >= MAX) {
                        return MAX;
                    }
                }
            }
        }
        
        return power;
    }
}
