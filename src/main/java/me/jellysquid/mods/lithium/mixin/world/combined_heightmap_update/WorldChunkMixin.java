package me.jellysquid.mods.lithium.mixin.world.combined_heightmap_update;

import me.jellysquid.mods.lithium.common.world.chunk.heightmap.CombinedHeightmapUpdate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;

@Mixin(LevelChunk.class)
public abstract class WorldChunkMixin extends ChunkAccess {
    public WorldChunkMixin(ChunkPos pos, UpgradeData upgradeData, LevelHeightAccessor heightLimitView, Registry<Biome> biome, long inhabitedTime, @Nullable LevelChunkSection[] sectionArrayInitializer, @Nullable BlendingData blendingData) {
        super(pos, upgradeData, heightLimitView, biome, inhabitedTime, sectionArrayInitializer, blendingData);
    }

    @Redirect(
            method = "setBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;")
    )
    private <K, V> V skipGetHeightmap(Map<K, V> heightmaps, K heightmapType) {
        if (heightmapType == Heightmap.Types.MOTION_BLOCKING || heightmapType == Heightmap.Types.MOTION_BLOCKING_NO_LEAVES || heightmapType == Heightmap.Types.OCEAN_FLOOR || heightmapType == Heightmap.Types.WORLD_SURFACE) {
            return null;
        }
        return heightmaps.get(heightmapType);
    }

    @Redirect(
            method = "setBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/Heightmap;update(IIILnet/minecraft/world/level/block/state/BlockState;)Z")
    )
    private boolean skipHeightmapUpdate(Heightmap instance, int x, int y, int z, BlockState state) {
        if (instance == null) {
            return false;
        }
        return instance.update(x, y, z, state);
    }

    @Inject(
            method = "setBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/levelgen/Heightmap;update(IIILnet/minecraft/world/level/block/state/BlockState;)Z",
                    shift = At.Shift.BEFORE,
                    ordinal = 0
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void updateHeightmapsCombined(BlockPos pos, BlockState state, boolean moved, CallbackInfoReturnable<BlockState> cir, int y, LevelChunkSection chunkSection, boolean bl, int x, int yMod16, int z, BlockState blockState, Block block) {
        Heightmap heightmap0 = this.heightmaps.get(Heightmap.Types.MOTION_BLOCKING);
        Heightmap heightmap1 = this.heightmaps.get(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES);
        Heightmap heightmap2 = this.heightmaps.get(Heightmap.Types.OCEAN_FLOOR);
        Heightmap heightmap3 = this.heightmaps.get(Heightmap.Types.WORLD_SURFACE);
        CombinedHeightmapUpdate.updateHeightmaps(heightmap0, heightmap1, heightmap2, heightmap3, (LevelChunk) (ChunkAccess) this, x, y, z, state);
    }
}
