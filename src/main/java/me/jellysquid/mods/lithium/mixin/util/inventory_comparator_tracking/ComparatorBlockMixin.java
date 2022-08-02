package me.jellysquid.mods.lithium.mixin.util.inventory_comparator_tracking;

import me.jellysquid.mods.lithium.common.block.entity.inventory_comparator_tracking.ComparatorTracking;
import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ComparatorBlock.class)
public abstract class ComparatorBlockMixin extends AbstractRedstoneGateBlock {
    protected ComparatorBlockMixin(Settings settings) {
        super(settings);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        super.onBlockAdded(state, world, pos, oldState, notify);
        if (!oldState.isOf(Blocks.COMPARATOR)) {
            ComparatorTracking.notifyNearbyBlockEntitiesAboutNewComparator(world, pos);
        }
    }
}
