package me.jellysquid.mods.lithium.common.world;

import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.util.collection.TypeFilterableList;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.WorldChunk;

import java.util.List;

public class WorldHelper {

    /**
     *  [VanillaCopy] Method for getting entities by class but also exclude one entity
     */
    public static List<Entity> getEntitiesOfClass(World world, Entity except, Class<? extends Entity> entityClass, Box box) {
        world.getProfiler().visit("getEntities");
        int i = MathHelper.floor((box.x1 - 2.0D) / 16.0D);
        int j = MathHelper.ceil((box.x2 + 2.0D) / 16.0D);
        int k = MathHelper.floor((box.z1 - 2.0D) / 16.0D);
        int l = MathHelper.ceil((box.z2 + 2.0D) / 16.0D);
        List<Entity> list = Lists.newArrayList();
        ChunkManager chunkManager = world.getChunkManager();

        for(int m = i; m < j; ++m) {
            for(int n = k; n < l; ++n) {
                WorldChunk worldChunk = chunkManager.getWorldChunk(m, n, false);
                if (worldChunk != null) {
                    WorldHelper.getEntitiesOfClass(worldChunk, except, entityClass, box, list);
                }
            }
        }

        return list;
    }

    /**
     *  [VanillaCopy] Method for getting entities by class but also exclude one entity
     */
    private static void getEntitiesOfClass(WorldChunk worldChunk, Entity excluded, Class<? extends Entity> entityClass, Box box, List<Entity> result) {
        TypeFilterableList<Entity>[] entitySections = worldChunk.getEntitySectionArray();
        int i = MathHelper.floor((box.y1 - 2.0D) / 16.0D);
        int j = MathHelper.floor((box.y2 + 2.0D) / 16.0D);
        i = MathHelper.clamp(i, 0, entitySections.length - 1);
        j = MathHelper.clamp(j, 0, entitySections.length - 1);

        for(int k = i; k <= j; ++k) {
            for (Entity entity : entitySections[k].getAllOfType(entityClass)) {
                if (entity != excluded && entity.getBoundingBox().intersects(box)) {
                    result.add(entity);
                }
            }
        }
    }
}
