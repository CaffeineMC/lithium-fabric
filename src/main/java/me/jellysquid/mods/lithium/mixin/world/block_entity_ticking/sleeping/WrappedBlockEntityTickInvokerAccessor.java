package me.jellysquid.mods.lithium.mixin.world.block_entity_ticking.sleeping;

import net.minecraft.world.level.block.entity.TickingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "net/minecraft/world/level/chunk/LevelChunk$RebindableTickingBlockEntityWrapper" )
public interface WrappedBlockEntityTickInvokerAccessor {
    @Invoker("rebind")
    void callSetWrapped(TickingBlockEntity wrapped);

    @Accessor("ticker")
    TickingBlockEntity getWrapped();
}
