package net.caffeinemc.mods.lithium.mixin.world.chunk_access;

import net.caffeinemc.mods.lithium.common.world.chunk.ChunkHolderExtended;
import net.minecraft.server.level.ChunkHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ChunkHolder.class)
public class ChunkHolderMixin implements ChunkHolderExtended {

    @Unique
    private long lastRequestTime;

    @Override
    public boolean lithium$updateLastAccessTime(long time) {
        long prev = this.lastRequestTime;
        this.lastRequestTime = time;

        return prev != time;
    }
}
