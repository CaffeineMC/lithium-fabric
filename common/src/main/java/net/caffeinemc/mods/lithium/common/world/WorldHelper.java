package net.caffeinemc.mods.lithium.common.world;

import net.caffeinemc.mods.lithium.common.client.ClientWorldAccessor;
import net.caffeinemc.mods.lithium.common.entity.EntityClassGroup;
import net.caffeinemc.mods.lithium.common.entity.pushable.EntityPushablePredicate;
import net.caffeinemc.mods.lithium.common.world.chunk.ClassGroupFilterableList;
import net.caffeinemc.mods.lithium.mixin.util.accessors.EntitySectionAccessor;
import net.caffeinemc.mods.lithium.mixin.util.accessors.PersistentEntitySectionManagerAccessor;
import net.caffeinemc.mods.lithium.mixin.util.accessors.ServerLevelAccessor;
import net.caffeinemc.mods.lithium.mixin.util.accessors.TransientEntitySectionManagerAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntitySectionStorage;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class WorldHelper {
    public static final boolean CUSTOM_TYPE_FILTERABLE_LIST_DISABLED = !ClassGroupFilterableList.class.isAssignableFrom(ClassInstanceMultiMap.class);

    /**
     * Partial [VanillaCopy]
     * The returned entity iterator is only used for collision interactions. As most entities do not collide with other
     * entities (cramming is different), getting them is not necessary. This is why we only get entities when they override
     * {@link Entity#canBeCollidedWith()} if the reference entity does not override {@link Entity#canCollideWith(Entity)}.
     * Note that the returned iterator contains entities that override these methods. This does not mean that these methods
     * always return true.
     *
     * @param entityView      the world
     * @param box             the box the entities have to collide with
     * @param collidingEntity the entity that is searching for the colliding entities
     * @return iterator of entities with collision boxes
     */
    public static List<Entity> getEntitiesForCollision(EntityGetter entityView, AABB box, Entity collidingEntity) {
        if (!CUSTOM_TYPE_FILTERABLE_LIST_DISABLED && entityView instanceof Level world && (collidingEntity == null || !EntityClassGroup.CUSTOM_COLLIDE_LIKE_MINECART_BOAT_WINDCHARGE.contains(collidingEntity.getClass()))) {
            EntitySectionStorage<Entity> cache = getEntityCacheOrNull(world);
            if (cache != null) {
                world.getProfiler().incrementCounter("getEntities");
                return getEntitiesOfClassGroup(cache, collidingEntity, EntityClassGroup.NoDragonClassGroup.BOAT_SHULKER_LIKE_COLLISION, box);
            }
        }
        //use vanilla code in case the shortcut is not applicable
        // due to the reference entity implementing special collision or the mixin being disabled in the config
        return entityView.getEntities(collidingEntity, box);
    }

    public static List<Entity> getOtherEntitiesForCollision(EntityGetter entityView, AABB box, @Nullable Entity collidingEntity, Predicate<? super Entity> entityPredicate) {
        if (!CUSTOM_TYPE_FILTERABLE_LIST_DISABLED && entityView instanceof Level world) {
            if (collidingEntity == null || !EntityClassGroup.CUSTOM_COLLIDE_LIKE_MINECART_BOAT_WINDCHARGE.contains(collidingEntity.getClass())) {
                EntitySectionStorage<Entity> cache = getEntityCacheOrNull(world);
                if (cache != null) {
                    world.getProfiler().incrementCounter("getEntities");
                    return getEntitiesOfClassGroup(cache, collidingEntity, EntityClassGroup.NoDragonClassGroup.BOAT_SHULKER_LIKE_COLLISION, box);
                }
            }
        }
        //use vanilla code in case the shortcut is not applicable
        // due to the reference entity implementing special collision or the mixin being disabled in the config
        return entityView.getEntities(collidingEntity, box, entityPredicate);
    }


    //Requires util.accessors
    public static EntitySectionStorage<Entity> getEntityCacheOrNull(Level world) {
        if (world instanceof ClientWorldAccessor) {
            //noinspection unchecked
            return ((TransientEntitySectionManagerAccessor<Entity>) ((ClientWorldAccessor) world).lithium$getEntityManager()).getCache();
        } else if (world instanceof ServerLevelAccessor) {
            //noinspection unchecked
            return ((PersistentEntitySectionManagerAccessor<Entity>) ((ServerLevelAccessor) world).getEntityManager()).getCache();
        }
        return null;
    }

    public static List<Entity> getEntitiesOfClassGroup(EntitySectionStorage<Entity> cache, Entity collidingEntity, EntityClassGroup.NoDragonClassGroup entityClassGroup, AABB box) {
        ArrayList<Entity> entities = new ArrayList<>();
        cache.forEachAccessibleNonEmptySection(box, section -> {
            //noinspection unchecked
            ClassInstanceMultiMap<Entity> allEntities = ((EntitySectionAccessor<Entity>) section).getCollection();
            //noinspection unchecked
            Collection<Entity> entitiesOfType = ((ClassGroupFilterableList<Entity>) allEntities).lithium$getAllOfGroupType(entityClassGroup);
            if (!entitiesOfType.isEmpty()) {
                for (Entity entity : entitiesOfType) {
                    if (entity.getBoundingBox().intersects(box) && !entity.isSpectator() && entity != collidingEntity) {
                        //skip the dragon piece check without issues by only allowing EntityClassGroup.NoDragonClassGroup as type
                        entities.add(entity);
                    }
                }
            }
            return AbortableIterationConsumer.Continuation.CONTINUE;
        });
        return entities;
    }

    public static List<Entity> getPushableEntities(Level world, EntitySectionStorage<Entity> cache, Entity except, AABB box, EntityPushablePredicate<? super Entity> entityPushablePredicate) {
        ArrayList<Entity> entities = new ArrayList<>();
        cache.forEachAccessibleNonEmptySection(box, section -> ((ClimbingMobCachingSection) section).lithium$collectPushableEntities(world, except, box, entityPushablePredicate, entities));
        return entities;
    }

    public static boolean areNeighborsWithinSameChunk(BlockPos pos) {
        int localX = pos.getX() & 15;
        int localZ = pos.getZ() & 15;

        return localX > 0 && localZ > 0 && localX < 15 && localZ < 15;
    }

    public static boolean areNeighborsWithinSameChunkSection(int x, int y, int z) {
        int localX = x & 15;
        int localY = y & 15;
        int localZ = z & 15;

        return localX > 0 && localY > 0 && localZ > 0 && localX < 15 && localY < 15 && localZ < 15;
    }

    public static boolean arePosWithinSameChunk(BlockPos pos1, BlockPos pos2) {
        return pos1.getX() >> 4 == pos2.getX() >> 4 && pos1.getZ() >> 4 == pos2.getZ() >> 4;
    }
}
