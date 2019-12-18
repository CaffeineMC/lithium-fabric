package me.jellysquid.mods.lithium.mixin.entity.chunk_cache;

import com.google.common.collect.Lists;
import me.jellysquid.mods.lithium.common.cache.EntityChunkCache;
import me.jellysquid.mods.lithium.common.entity.EntityWithChunkCache;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EntityView;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.function.Predicate;

@SuppressWarnings("OverwriteModifiers")
@Mixin(EntityView.class)
public interface MixinEntityView {
    @Shadow
    <T extends Entity> List<T> getEntitiesIncludingUngeneratedChunks(Class<? extends T> entityClass, Box box, Predicate<? super T> predicate);

    @Shadow
    <T extends LivingEntity> T getClosestEntity(List<? extends T> entityList, TargetPredicate targetPredicate, LivingEntity entity, double x, double y, double z);

    /**
     * @reason Use the entity's chunk cache if possible
     * @author JellySquid
     */
    @Overwrite
    default <T extends LivingEntity> T getClosestEntityIncludingUngeneratedChunks(Class<? extends T> entityClass, TargetPredicate targetPredicate, LivingEntity entity, double x, double y, double z, Box box) {
        List<T> entities;

        if (this instanceof World) {
            entities = this.getEntitiesIncludingUngeneratedChunks(entity, entityClass, box, null);
        } else {
            entities = this.getEntitiesIncludingUngeneratedChunks(entityClass, box, null);
        }

        return this.getClosestEntity(entities, targetPredicate, entity, x, y, z);
    }

    default <T extends Entity> List<T> getEntitiesIncludingUngeneratedChunks(Entity entity, Class<? extends T> entityClass, Box box, Predicate<? super T> predicate) {
        EntityChunkCache cache = entity instanceof EntityWithChunkCache ? ((EntityWithChunkCache) entity).getEntityChunkCache() : null;

        int minX = MathHelper.floor((box.x1 - 2.0D) / 16.0D);
        int maxX = MathHelper.ceil((box.x2 + 2.0D) / 16.0D);
        int minZ = MathHelper.floor((box.z1 - 2.0D) / 16.0D);
        int maxZ = MathHelper.ceil((box.z2 + 2.0D) / 16.0D);

        List<T> list = Lists.newArrayList();

        ChunkManager chunkManager = ((World) this).getChunkManager();

        for (int x = minX; x < maxX; ++x) {
            for (int z = minZ; z < maxZ; ++z) {
                WorldChunk chunk = cache != null ? cache.getChunk(x, z) : chunkManager.getWorldChunk(x, z);

                if (chunk != null) {
                    chunk.getEntities(entityClass, box, list, predicate);
                }
            }
        }

        return list;
    }

}
