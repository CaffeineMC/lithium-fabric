package me.jellysquid.mods.lithium.common.block.entity;

import me.jellysquid.mods.lithium.mixin.world.block_entity_ticking.sleeping.WrappedBlockEntityTickInvokerAccessor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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

    WrappedBlockEntityTickInvokerAccessor lithium$getTickWrapper();

    void lithium$setTickWrapper(WrappedBlockEntityTickInvokerAccessor tickWrapper);

    BlockEntityTickInvoker lithium$getSleepingTicker();

    void lithium$setSleepingTicker(BlockEntityTickInvoker sleepingTicker);

    default boolean lithium$startSleeping() {
        if (this.isSleeping()) {
            return false;
        }

        WrappedBlockEntityTickInvokerAccessor tickWrapper = this.lithium$getTickWrapper();
        if (tickWrapper == null) {
            return false;
        }
        this.lithium$setSleepingTicker(tickWrapper.getWrapped());
        tickWrapper.callSetWrapped(SleepingBlockEntity.SLEEPING_BLOCK_ENTITY_TICKER);
        return true;
    }

    default void sleepOnlyCurrentTick() {
        BlockEntityTickInvoker sleepingTicker = this.lithium$getSleepingTicker();
        WrappedBlockEntityTickInvokerAccessor tickWrapper = this.lithium$getTickWrapper();
        if (sleepingTicker == null) {
            sleepingTicker = tickWrapper.getWrapped();
        }
        World world = ((BlockEntity) this).getWorld();
        tickWrapper.callSetWrapped(new SleepUntilTimeBlockEntityTickInvoker((BlockEntity) this, world.getTime() + 1, sleepingTicker));
        this.lithium$setSleepingTicker(null);
    }

    default void wakeUpNow() {
        BlockEntityTickInvoker sleepingTicker = this.lithium$getSleepingTicker();
        if (sleepingTicker == null) {
            return;
        }
        this.setTicker(sleepingTicker);
        this.lithium$setSleepingTicker(null);
    }

    default void setTicker(BlockEntityTickInvoker delegate) {
        WrappedBlockEntityTickInvokerAccessor tickWrapper = this.lithium$getTickWrapper();
        if (tickWrapper == null) {
            return;
        }
        tickWrapper.callSetWrapped(delegate);
    }

    default boolean isSleeping() {
        return this.lithium$getSleepingTicker() != null;
    }
}
