package me.jellysquid.mods.lithium.mixin.entity.chunk_cache;

import com.google.common.collect.Lists;
import me.jellysquid.mods.lithium.common.cache.EntityChunkCache;
import me.jellysquid.mods.lithium.common.entity.ExtendedEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.function.Predicate;

@Mixin(World.class)
public abstract class MixinWorld {
    @Shadow
    public abstract ChunkManager getChunkManager();

    /**
     * @reason Use the entity parameter to access a nearby chunk cache
     * @author JellySquid
     */
    @Overwrite
    public List<Entity> getEntities(Entity entity, Box box, Predicate<? super Entity> predicate) {
        EntityChunkCache cache = entity instanceof ExtendedEntity ? ((ExtendedEntity) entity).getEntityChunkCache() : null;

        List<Entity> ret = Lists.newArrayList();

        int minX = MathHelper.floor((box.minX - 2.0D) / 16.0D);
        int maxX = MathHelper.floor((box.maxX + 2.0D) / 16.0D);
        int minZ = MathHelper.floor((box.minZ - 2.0D) / 16.0D);
        int maxZ = MathHelper.floor((box.maxZ + 2.0D) / 16.0D);

        for (int x = minX; x <= maxX; ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                WorldChunk chunk = cache != null ? cache.getChunk(x, z) : this.getChunkManager().getWorldChunk(x, z, false);

                if (chunk != null) {
                    chunk.appendEntities(entity, box, ret, predicate);
                }
            }
        }

        return ret;
    }


}
