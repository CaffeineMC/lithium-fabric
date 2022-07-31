package me.jellysquid.mods.lithium.common.block.entity;

import me.jellysquid.mods.lithium.mixin.world.block_entity_ticking.sleeping.WrappedBlockEntityTickInvokerAccessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.BlockEntityTickInvoker;

public interface SleepingBlockEntity {
    BlockEntityTickInvoker SLEEPING_BLOCK_ENTITY_TICKER = new BlockEntityTickInvoker() {
        public void tick() {
        }

        public boolean isRemoved() {
            return false;
        }

        public BlockPos getPos() {
            return null;
        }

        public String getName() {
            return "<lithium_sleeping>";
        }
    };


    void setWrappedInvoker(WrappedBlockEntityTickInvokerAccessor wrappedBlockEntityTickInvoker);

    void setTicker(BlockEntityTickInvoker delegate);
}
