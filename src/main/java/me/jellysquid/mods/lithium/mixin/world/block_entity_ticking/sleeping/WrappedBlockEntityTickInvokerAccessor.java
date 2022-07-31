package me.jellysquid.mods.lithium.mixin.world.block_entity_ticking.sleeping;

import net.minecraft.world.chunk.BlockEntityTickInvoker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net/minecraft/world/chunk/WorldChunk$WrappedBlockEntityTickInvoker" )
public interface WrappedBlockEntityTickInvokerAccessor {
    @Accessor
    void setWrapped(BlockEntityTickInvoker wrapped);

    @Accessor
    BlockEntityTickInvoker getWrapped();
}
