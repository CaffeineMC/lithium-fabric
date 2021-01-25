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
    public interface MixinLoadTest {
    }

    public static final boolean CUSTOM_TYPE_FILTERABLE_LIST_DISABLED = !MixinLoadTest.class.isAssignableFrom(TypeFilterableList.class);
    public static final Predicate<Entity> BOAT_SHULKER_LIKE_COLLISION_PREDICATE = (entity) -> EntityClassGroup.BOAT_SHULKER_LIKE_COLLISION.contains(entity.getClass()) && !entity.isSpectator();


    /**
     * Partial [VanillaCopy] Classes overriding Entity.getHardCollisionBox(Entity other) or Entity.getCollisionBox()
     * The returned entity list is only used to call getCollisionBox and getHardCollisionBox. As most entities return null
     * for both of these methods, getting those is not necessary. This is why we only get entities when they overwrite
     * getCollisionBox
     *
     * @param entityView      the world
     * @param box             the box the entities have to collide with
     * @param collidingEntity the entity that is searching for the colliding entities
     * @return list of entities with collision boxes
     */
    public static List<Entity> getEntitiesWithCollisionBoxForEntity(EntityView entityView, Box box, Entity collidingEntity) {
        if (CUSTOM_TYPE_FILTERABLE_LIST_DISABLED || collidingEntity != null && EntityClassGroup.MINECART_BOAT_LIKE_COLLISION.contains(collidingEntity.getClass()) || !(entityView instanceof World)) {
            //use vanilla code when method_30949 (previously getHardCollisionBox(Entity other)) is overwritten, as every entity could be relevant as argument of getHardCollisionBox
            return entityView.getOtherEntities(collidingEntity, box);
        } else {
            //only get entities that overwrite method_30948 (previously getCollisionBox)
            //todo optimized implementation
            return entityView.getOtherEntities(collidingEntity, box, BOAT_SHULKER_LIKE_COLLISION_PREDICATE);
        }
    }



    public static boolean areNeighborsWithinSameChunk(BlockPos pos) {
        int localX = pos.getX() & 15;
        int localY = pos.getY() & 15;
        int localZ = pos.getZ() & 15;

        return localX > 0 && localY > 0 && localZ > 0 && localX < 15 && localY < 15 && localZ < 15;
    }
}
