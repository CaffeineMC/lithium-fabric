package me.jellysquid.mods.lithium.common.world;

import com.google.common.collect.Lists;
import me.jellysquid.mods.lithium.common.entity.EntityClassGroup;
import me.jellysquid.mods.lithium.common.world.chunk.ClassGroupFilterableList;
import net.minecraft.entity.Entity;
import net.minecraft.util.collection.TypeFilterableList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EntityView;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.WorldChunk;

import java.util.List;
import java.util.function.Predicate;

import static net.minecraft.predicate.entity.EntityPredicates.EXCEPT_SPECTATOR;

public class WorldHelper {
    public interface MixinLoadTest {}
    public static final boolean CUSTOM_TYPE_FILTERABLE_LIST_DISABLED = !MixinLoadTest.class.isAssignableFrom(TypeFilterableList.class);


    /**
     * Partial [VanillaCopy] Classes overriding Entity.getHardCollisionBox(Entity other) or Entity.getCollisionBox()
     * The returned entity list is only used to call getCollisionBox and getHardCollisionBox. As most entities return null
     * for both of these methods, getting those is not necessary. This is why we only get entities when they overwrite
     * getCollisionBox
     * @param entityView the world
     * @param box the box the entities have to collide with
     * @param collidingEntity the entity that is searching for the colliding entities
     * @return list of entities with collision boxes
     */
    public static List<Entity> getEntitiesWithCollisionBoxForEntity(EntityView entityView, Box box, Entity collidingEntity) {
        if (CUSTOM_TYPE_FILTERABLE_LIST_DISABLED || collidingEntity != null && EntityClassGroup.MINECART_BOAT_LIKE_COLLISION.contains(collidingEntity.getClass()) || !(entityView instanceof World)) {
            //use vanilla code when method_30949 (previously getHardCollisionBox(Entity other)) is overwritten, as every entity could be relevant as argument of getHardCollisionBox
            return entityView.getOtherEntities(collidingEntity, box);
        } else {
            //only get entities that overwrite method_30948 (previously getCollisionBox)
            return getEntitiesOfClassGroup((World)entityView, collidingEntity, EntityClassGroup.BOAT_SHULKER_LIKE_COLLISION, box, EXCEPT_SPECTATOR);
        }
    }

    /**
     * Method that allows getting entities of a class group.
     * [VanillaCopy] but custom combination of: get class filtered entities together with excluding one entity
     */
    public static List<Entity> getEntitiesOfClassGroup(World world, Entity excluded, EntityClassGroup type, Box box_1, Predicate<Entity> predicate_1) {
        world.getProfiler().visit("getEntities");
        int int_1 = MathHelper.floor((box_1.minX - 2.0D) / 16.0D);
        int int_2 = MathHelper.ceil((box_1.maxX + 2.0D) / 16.0D);
        int int_3 = MathHelper.floor((box_1.minZ - 2.0D) / 16.0D);
        int int_4 = MathHelper.ceil((box_1.maxZ + 2.0D) / 16.0D);
        List<Entity> list_1 = Lists.newArrayList();
        ChunkManager chunkManager_1 = world.getChunkManager();

        for(int int_5 = int_1; int_5 < int_2; ++int_5) {
            for(int int_6 = int_3; int_6 < int_4; ++int_6) {
                WorldChunk worldChunk_1 = chunkManager_1.getWorldChunk(int_5, int_6, false);
                if (worldChunk_1 != null) {
                    WorldHelper.getEntitiesOfClassGroup(worldChunk_1, excluded, type, box_1, list_1, predicate_1);
                }
            }
        }

        return list_1;
    }
    /**
     * Method that allows getting entities of a class group.
     * [VanillaCopy] but custom combination of: get class filtered entities together with excluding one entity
     */
    public static void getEntitiesOfClassGroup(WorldChunk worldChunk, Entity excluded, EntityClassGroup type, Box box_1, List<Entity> list_1, Predicate<Entity> predicate_1) {
        TypeFilterableList<Entity>[] entitySections = worldChunk.getEntitySectionArray();
        int int_1 = MathHelper.floor((box_1.minY - 2.0D) / 16.0D);
        int int_2 = MathHelper.floor((box_1.maxY + 2.0D) / 16.0D);
        int_1 = MathHelper.clamp(int_1, 0, entitySections.length - 1);
        int_2 = MathHelper.clamp(int_2, 0, entitySections.length - 1);

        for(int int_3 = int_1; int_3 <= int_2; ++int_3) {
            //noinspection rawtypes
            for(Object entity_1 : ((ClassGroupFilterableList)entitySections[int_3]).getAllOfGroupType(type)) {
                if (entity_1 != excluded && ((Entity)entity_1).getBoundingBox().intersects(box_1) && (predicate_1 == null || predicate_1.test((Entity)entity_1))) {
                    list_1.add((Entity)entity_1);
                }
            }
        }
    }


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

    public static boolean areNeighborsWithinSameChunk(BlockPos pos) {
        int localX = pos.getX() & 15;
        int localY = pos.getY() & 15;
        int localZ = pos.getZ() & 15;

        return localX > 0 && localY > 0 && localZ > 0 && localX < 15 && localY < 15 && localZ < 15;
    }
}
