package me.jellysquid.mods.lithium.mixin.util.block_entity_retrieval;

import me.jellysquid.mods.lithium.common.world.blockentity.BlockEntityGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Level.class)
public abstract class WorldMixin implements BlockEntityGetter, LevelAccessor {
    @Shadow
    @Final
    public boolean isClientSide;

    @Shadow
    @Final
    private Thread thread;

    @Shadow
    public abstract LevelChunk getChunk(int i, int j);

    @Shadow
    @Nullable
    public abstract ChunkAccess getChunk(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create);

    @Override
    public BlockEntity lithium$getLoadedExistingBlockEntity(BlockPos pos) {
        if (!this.isOutsideBuildHeight(pos)) {
            if (this.isClientSide || Thread.currentThread() == this.thread) {
                ChunkAccess chunk = this.getChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()), ChunkStatus.FULL, false);
                if (chunk != null) {
                    return chunk.getBlockEntity(pos);
                }
            }
        }
        return null;
    }
}
