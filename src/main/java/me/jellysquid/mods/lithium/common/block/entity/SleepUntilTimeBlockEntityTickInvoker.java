package me.jellysquid.mods.lithium.common.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TickingBlockEntity;

public record SleepUntilTimeBlockEntityTickInvoker(BlockEntity sleepingBlockEntity, long sleepUntilTickExclusive,
                                                   TickingBlockEntity delegate) implements TickingBlockEntity {

    @Override
    public void tick() {
        //noinspection ConstantConditions
        long tickTime = this.sleepingBlockEntity.getLevel().getGameTime();
        if (tickTime >= this.sleepUntilTickExclusive) {
            ((SleepingBlockEntity) this.sleepingBlockEntity).setTicker(this.delegate);
            this.delegate.tick();
        }
    }

    @Override
    public boolean isRemoved() {
        return this.sleepingBlockEntity.isRemoved();
    }

    @Override
    public BlockPos getPos() {
        return this.sleepingBlockEntity.getBlockPos();
    }

    @Override
    public String getType() {
        //noinspection ConstantConditions
        return BlockEntityType.getKey(this.sleepingBlockEntity.getType()).toString();
    }
}
