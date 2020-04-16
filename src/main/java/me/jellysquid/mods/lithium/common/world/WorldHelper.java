package me.jellysquid.mods.lithium.common.world;

import com.google.common.collect.Lists;
import me.jellysquid.mods.lithium.common.entity.EntityClassGroup;
import net.minecraft.entity.Entity;
import net.minecraft.util.collection.TypeFilterableList;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.WorldChunk;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class WorldHelper {


    /**
     * Method that allows getting entities of a class that does not extend Entity. This makes it possible to
     * group entity classes using interfaces.
     * [VanillaCopy] but custom combination of: get class filtered entities together with excluding one entity
     */
    public static List<Entity> getEntitiesOfClassGroup(World world, Entity excluded, EntityClassGroup type, Box box_1, Predicate<Entity> predicate_1) {
        world.getProfiler().visit("getEntities");
        int int_1 = MathHelper.floor((box_1.x1 - 2.0D) / 16.0D);
        int int_2 = MathHelper.ceil((box_1.x2 + 2.0D) / 16.0D);
        int int_3 = MathHelper.floor((box_1.z1 - 2.0D) / 16.0D);
        int int_4 = MathHelper.ceil((box_1.z2 + 2.0D) / 16.0D);
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
    //mostly [VanillaCopy] but custom combination of: get class filtered entities together with excluding one entity
    public static void getEntitiesOfClassGroup(WorldChunk chunk, Entity excluded, EntityClassGroup type, Box box_1, List<Entity> list_1, Predicate<Entity> predicate_1) {
        TypeFilterableList<Entity>[] entitySections = chunk.getEntitySectionArray();
        int int_1 = MathHelper.floor((box_1.y1 - 2.0D) / 16.0D);
        int int_2 = MathHelper.floor((box_1.y2 + 2.0D) / 16.0D);
        int_1 = MathHelper.clamp(int_1, 0, entitySections.length - 1);
        int_2 = MathHelper.clamp(int_2, 0, entitySections.length - 1);

        for(int int_3 = int_1; int_3 <= int_2; ++int_3) {
            for(Object entity_1 : ((ClassGroupFilterableList)entitySections[int_3]).getAllOfGroupType(type)) {
                if (entity_1 != excluded && ((Entity)entity_1).getBoundingBox().intersects(box_1) && (predicate_1 == null || predicate_1.test((Entity)entity_1))) {
                    list_1.add((Entity)entity_1);
                }
            }
        }
    }

    public interface ClassGroupFilterableList<T> {
        Collection<T> getAllOfGroupType(EntityClassGroup type);
    }
}
