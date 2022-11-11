package me.jellysquid.mods.lithium.mixin.world.block_entity_ticking.sleeping;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(World.class)
public class WorldMixin {

    @Redirect(
            method = "tickBlockEntities",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;shouldTickBlocksInChunk(J)Z" )
    )
    private boolean shouldTickBlockPosFilterNull(World instance, long chunkPos) {
        if (chunkPos == Long.MIN_VALUE) {
            return false;
        }
        return instance.shouldTickBlocksInChunk(chunkPos);
    }

    @Redirect(
            method = "tickBlockEntities",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/ChunkPos;toLong(Lnet/minecraft/util/math/BlockPos;)J" )
    )
    private long shouldTickBlockPosFilterNull(BlockPos pos) {
        if (pos == null) {
            return Long.MIN_VALUE;
        }
        return ChunkPos.toLong(pos);
    }
}
