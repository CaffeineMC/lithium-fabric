package me.jellysquid.mods.lithium.common.world;

import me.jellysquid.mods.lithium.common.entity.EntityClassGroup;
import me.jellysquid.mods.lithium.common.world.chunk.ClassGroupFilterableList;
import me.jellysquid.mods.lithium.mixin.chunk.entity_class_groups.EntityTrackingSectionAccessor;
import me.jellysquid.mods.lithium.mixin.chunk.entity_class_groups.ServerEntityManagerAccessor;
import me.jellysquid.mods.lithium.mixin.chunk.entity_class_groups.ServerWorldAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.TypeFilterableList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.EntityView;
import net.minecraft.world.entity.EntityTrackingSection;
import net.minecraft.world.entity.SectionedEntityCache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WorldHelper {
    public static final boolean CUSTOM_TYPE_FILTERABLE_LIST_DISABLED = !ClassGroupFilterableList.class.isAssignableFrom(TypeFilterableList.class);

    /**
     * Partial [VanillaCopy]
     * The returned entity iterator is only used for collision interactions. As most entities do not collide with other
     * entities (cramming is different), getting those is not necessary. This is why we only get entities when they override
     * {@link Entity#isCollidable()} if the reference entity does not override {@link Entity#collidesWith(Entity)}.
     * Note that the returned iterator contains entities that override these methods. This does not mean that these methods
     * always return true.
     *
     * @param entityView      the world
     * @param box             the box the entities have to collide with
     * @param collidingEntity the entity that is searching for the colliding entities
     * @return iterator of entities with collision boxes
     */
    public static List<Entity> getEntitiesForCollision(EntityView entityView, Box box, Entity collidingEntity) {
        if (CUSTOM_TYPE_FILTERABLE_LIST_DISABLED || !(entityView instanceof ServerWorld) || collidingEntity != null && EntityClassGroup.MINECART_BOAT_LIKE_COLLISION.contains(collidingEntity.getClass())) {
            //use vanilla code in case the shortcut is not applicable
            // due to the reference entity implementing special collision or the mixin being disabled in the config
            return entityView.getOtherEntities(collidingEntity, box);
        } else {
            return getEntitiesOfClassGroup((ServerWorld) entityView, collidingEntity, EntityClassGroup.NoDragonClassGroup.BOAT_SHULKER_LIKE_COLLISION, box);
        }
    }

    public static List<Entity> getEntitiesOfClassGroup(ServerWorld world, Entity collidingEntity, EntityClassGroup.NoDragonClassGroup entityClassGroup, Box box) {
        world.getProfiler().visit("getEntities");
        //noinspection unchecked
        SectionedEntityCache<Entity> cache = ((ServerEntityManagerAccessor<Entity>) ((ServerWorldAccessor) world).getEntityManager()).getCache();
        final int minX = ChunkSectionPos.getSectionCoord(box.minX - 2.0D);
        final int minY = ChunkSectionPos.getSectionCoord(box.minY - 2.0D);
        final int minZ = ChunkSectionPos.getSectionCoord(box.minZ - 2.0D);
        final int maxX = ChunkSectionPos.getSectionCoord(box.maxX + 2.0D);
        final int maxY = ChunkSectionPos.getSectionCoord(box.maxY + 2.0D);
        final int maxZ = ChunkSectionPos.getSectionCoord(box.maxZ + 2.0D);
        ArrayList<Entity> entities = new ArrayList<>();
        //todo fix iteration order
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    EntityTrackingSection<Entity> section = cache.findTrackingSection(ChunkSectionPos.asLong(x, y, z));
                    if (section != null) {
                        //noinspection unchecked
                        TypeFilterableList<Entity> allEntities = ((EntityTrackingSectionAccessor<Entity>) section).getCollection();
                        if (!allEntities.isEmpty()) {
                            //noinspection unchecked
                            Collection<Entity> entitiesOfType = ((ClassGroupFilterableList<Entity>) allEntities).getAllOfGroupType(entityClassGroup);
                            if (!entitiesOfType.isEmpty()) {
                                for (Entity entity : entitiesOfType) {
                                    if (entity.getBoundingBox().intersects(box) && !entity.isSpectator() && entity != collidingEntity) {
                                        //skip the dragon piece check without issues by only allowing only EntityClassGroup.NoDragonClassGroup as type
                                        entities.add(entity);
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }
        return entities;
    }

    public static boolean areNeighborsWithinSameChunk(BlockPos pos) {
        int localX = pos.getX() & 15;
        int localY = pos.getY() & 15;
        int localZ = pos.getZ() & 15;

        return localX > 0 && localY > 0 && localZ > 0 && localX < 15 && localY < 15 && localZ < 15;
    }
}
