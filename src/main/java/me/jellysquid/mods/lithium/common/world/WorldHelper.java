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
        int chunkX1 = MathHelper.floor((box.minX - 2.0D) / 16.0D);
        int chunkX2 = MathHelper.ceil((box.maxX + 2.0D) / 16.0D);
        int chunkZ1 = MathHelper.floor((box.minZ - 2.0D) / 16.0D);
        int chunkZ2 = MathHelper.ceil((box.maxZ + 2.0D) / 16.0D);
        List<Entity> entityList = Lists.newArrayList();
        ChunkManager chunkManager = world.getChunkManager();

        for(int chunkX = chunkX1; chunkX < chunkX2; ++chunkX) {
            for(int chunkZ = chunkZ1; chunkZ < chunkZ2; ++chunkZ) {
                WorldChunk worldChunk = chunkManager.getWorldChunk(chunkX, chunkZ, false);
                if (worldChunk != null) {
                    WorldHelper.getEntitiesOfClass(worldChunk, except, entityClass, box, entityList);
                }
            }
        }

        return entityList;
    }

    /**
     *  [VanillaCopy] Method for getting entities by class but also exclude one entity
     */
    private static void getEntitiesOfClass(WorldChunk worldChunk, Entity excluded, Class<? extends Entity> entityClass, Box box, List<Entity> entityList) {
        TypeFilterableList<Entity>[] entitySections = worldChunk.getEntitySectionArray();
        int chunkY1 = MathHelper.floor((box.minY - 2.0D) / 16.0D);
        int chunkY2 = MathHelper.floor((box.maxY + 2.0D) / 16.0D);
        chunkY1 = MathHelper.clamp(chunkY1, 0, entitySections.length - 1);
        chunkY2 = MathHelper.clamp(chunkY2, 0, entitySections.length - 1);

        for(int chunkY = chunkY1; chunkY <= chunkY2; ++chunkY) {
            for (Entity entity : entitySections[chunkY].getAllOfType(entityClass)) {
                if (entity != excluded && entity.getBoundingBox().intersects(box)) {
                    entityList.add(entity);
                }
            }
        }
    }
}
