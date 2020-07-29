package me.jellysquid.mods.lithium.mixin.entity.slimechunk_cache;

import org.spongepowered.asm.mixin.Mixin;

import me.jellysquid.mods.lithium.common.chunk.ChunkWithSlimeTag;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.ReadOnlyChunk;
import net.minecraft.world.chunk.UpgradeData;

@Mixin(ReadOnlyChunk.class)
public abstract class ReadOnlyChunkMixin extends ProtoChunk implements ChunkWithSlimeTag {

    public ReadOnlyChunkMixin(ChunkPos pos, UpgradeData upgradeData) {
        super(pos, upgradeData);
    }

    public boolean isSlime = false;

    @Override
    public boolean isSlimeChunk() {
        return isSlime;
    }

    @Override
    public void setSlimeChunk(boolean flag) {
        this.isSlime = flag;
    }
}