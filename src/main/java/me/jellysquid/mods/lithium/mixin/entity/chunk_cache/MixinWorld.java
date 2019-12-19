package me.jellysquid.mods.lithium.mixin.entity.chunk_cache;

import me.jellysquid.mods.lithium.common.cache.EntityChunkCache;
import me.jellysquid.mods.lithium.common.entity.EntityWithChunkCache;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Predicate;

@Mixin(World.class)
public abstract class MixinWorld {
    @Redirect(method = "getEntities(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;)Ljava/util/List;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/ChunkManager;getWorldChunk(IIZ)Lnet/minecraft/world/chunk/WorldChunk;"))
    private WorldChunk redirectGetWorldChunk(ChunkManager chunkManager, int chunkX, int chunkZ, boolean create, Entity entity, Box box, Predicate<? super Entity> predicate) {
        EntityChunkCache cache = EntityWithChunkCache.getChunkCache(entity);

        return cache != null ? cache.getChunk(chunkX, chunkZ) : chunkManager.getWorldChunk(chunkX, chunkZ);
    }
}
