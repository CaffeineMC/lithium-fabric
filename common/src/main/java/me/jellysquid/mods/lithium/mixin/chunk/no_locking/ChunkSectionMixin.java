package me.jellysquid.mods.lithium.mixin.chunk.no_locking;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LevelChunkSection.class)
public abstract class ChunkSectionMixin {

    @Shadow
    public abstract BlockState setBlockState(int x, int y, int z, BlockState state, boolean lock);

    @Redirect(
            method = "setBlockState(IIILnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunkSection;setBlockState(IIILnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/world/level/block/state/BlockState;")
    )
    private BlockState setBlockStateNoLocking(LevelChunkSection chunkSection, int x, int y, int z, BlockState state, boolean lock) {
        return this.setBlockState(x, y, z, state, false);
    }
}
