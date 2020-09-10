package me.jellysquid.mods.lithium.mixin.alloc.composter;

import net.minecraft.inventory.SidedInventory;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

public class ComposterMixin {
    private static final int[] EMPTY = new int[0];
    private static final int[] ZERO = new int[]{0};

    @Mixin(targets = "net.minecraft.block.ComposterBlock$ComposterInventory")
    public static abstract class ComposterBlockComposterInventoryMixin implements SidedInventory {
        /**
         * @author 2No2Name
         * @reason avoid allocation
         */
        @Overwrite
        public int[] getAvailableSlots(Direction side) {
            return side == Direction.UP ? ZERO : EMPTY;
        }
    }

    @Mixin(targets = "net.minecraft.block.ComposterBlock$DummyInventory")
    public static abstract class ComposterBlockDummyInventoryMixin implements SidedInventory {
        /**
         * @author 2No2Name
         * @reason avoid allocation
         */
        @Overwrite
        public int[] getAvailableSlots(Direction side) {
            return EMPTY;
        }
    }

    @Mixin(targets = "net.minecraft.block.ComposterBlock$FullComposterInventory")
    public static abstract class ComposterBlockFullComposterInventoryMixin implements SidedInventory {
        /**
         * @author 2No2Name
         * @reason avoid allocation
         */
        @Overwrite
        public int[] getAvailableSlots(Direction side) {
            return side == Direction.DOWN ? ZERO : EMPTY;
        }
    }
}
