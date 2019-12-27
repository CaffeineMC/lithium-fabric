package me.jellysquid.mods.lithium.mixin.redstone;

import me.jellysquid.mods.lithium.common.block.redstone.RedstoneEngine;
import me.jellysquid.mods.lithium.common.block.redstone.WorldWithRedstoneEngine;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(RedstoneWireBlock.class)
public abstract class MixinRedstoneWireBlock {
    /**
     * @reason Use faster implementation
     * @author JellySquid
     */
    @Overwrite
    private BlockState update(World world, BlockPos pos, BlockState state) {
        RedstoneEngine engine = ((WorldWithRedstoneEngine) world).getRedstoneEngine();
        engine.notifyWireChange(pos, state.get(RedstoneWireBlock.POWER));

        return state;
    }

    /**
     * @reason Use faster implementation
     * @author JellySquid
     */
    @Overwrite
    public void onBlockRemoved(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() == newState.getBlock()) {
            return;
        }

        RedstoneEngine engine = ((WorldWithRedstoneEngine) world).getRedstoneEngine();
        engine.notifyWireRemoved(pos);
    }

    /**
     * @reason Use faster implementation
     * @author JellySquid
     */
    @Overwrite
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean moved) {
        if (state.getBlock() == oldState.getBlock()) {
            return;
        }

        RedstoneEngine engine = ((WorldWithRedstoneEngine) world).getRedstoneEngine();
        engine.notifyWireAdded(pos);
    }

}