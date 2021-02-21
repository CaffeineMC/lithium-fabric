package me.jellysquid.mods.lithium.mixin.world.block_entity_ticking.sleeping;

import me.jellysquid.mods.lithium.common.world.blockentity.SleepingBlockEntity;
import net.minecraft.block.entity.DaylightDetectorBlockEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DaylightDetectorBlockEntity.class)
public class DaylightDetectorBlockEntityMixin implements SleepingBlockEntity {
    @Override
    public boolean canTickOnSide(boolean isClient) {
        return !isClient;
    }
}