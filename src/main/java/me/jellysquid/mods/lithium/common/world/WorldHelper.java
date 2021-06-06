package me.jellysquid.mods.lithium.common.world;

import com.google.common.collect.AbstractIterator;
import me.jellysquid.mods.lithium.common.entity.EntityClassGroup;
import me.jellysquid.mods.lithium.common.world.chunk.ClassGroupFilterableList;
import me.jellysquid.mods.lithium.mixin.chunk.entity_class_groups.EntityTrackingSectionAccessor;
import me.jellysquid.mods.lithium.mixin.chunk.entity_class_groups.ServerEntityManagerAccessor;
import me.jellysquid.mods.lithium.mixin.chunk.entity_class_groups.ServerWorldAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.collection.TypeFilterableList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.EntityView;
import net.minecraft.world.World;
import net.minecraft.world.entity.EntityTrackingSection;
import net.minecraft.world.entity.SectionedEntityCache;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

public class WorldHelper {
    public static final boolean CUSTOM_TYPE_FILTERABLE_LIST_DISABLED = !ClassGroupFilterableList.class.isAssignableFrom(TypeFilterableList.class);

    public static final TypeFilter<Entity, Entity> UNFILTERED = new TypeFilter<>() {
        @Nullable
        @Override
        public Entity downcast(Entity obj) {
            return obj;
        }

        @Override
        public Class<? extends Entity> getBaseClass() {
            return Entity.class;
        }
    };

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
    public static Iterator<Entity> getEntitiesForCollision(EntityView entityView, Box box, Entity collidingEntity) {
        if (CUSTOM_TYPE_FILTERABLE_LIST_DISABLED || !(entityView instanceof ServerWorld) || collidingEntity != null && EntityClassGroup.MINECART_BOAT_LIKE_COLLISION.contains(collidingEntity.getClass()) || !(entityView instanceof World)) {
            //use vanilla code in case the shortcut is not applicable
            // due to the reference entity implementing special collision or the mixin being disabled in the config
            return entityView.getOtherEntities(collidingEntity, box).iterator();
        } else {
            return iterateEntitiesOfClassGroupExceptSpectator((ServerWorld) entityView, collidingEntity, EntityClassGroup.NoDragonClassGroup.BOAT_SHULKER_LIKE_COLLISION, box);
        }
    }

    public static Iterator<Entity> iterateEntitiesOfClassGroupExceptSpectator(ServerWorld world, Entity collidingEntity, EntityClassGroup.NoDragonClassGroup entityClassGroup, Box box) {
        //noinspection unchecked
        SectionedEntityCache<Entity> cache = ((ServerEntityManagerAccessor<Entity>) ((ServerWorldAccessor) world).getEntityManager()).getCache();
        return new AbstractIterator<>() {
            final SectionedEntityCache<Entity> sectionedEntityCache = cache;
            final Box location = box;
            final EntityClassGroup.NoDragonClassGroup type = entityClassGroup;
            final Entity except = collidingEntity;

            final int minX = ChunkSectionPos.getSectionCoord(this.location.minX - 2.0D);
            final int minY = ChunkSectionPos.getSectionCoord(this.location.minY - 2.0D);
            final int minZ = ChunkSectionPos.getSectionCoord(this.location.minZ - 2.0D);
            final int maxX = ChunkSectionPos.getSectionCoord(this.location.maxX + 2.0D);
            final int maxY = ChunkSectionPos.getSectionCoord(this.location.maxY + 2.0D);
            final int maxZ = ChunkSectionPos.getSectionCoord(this.location.maxZ + 2.0D);

            int sectionX = this.minX, sectionZ = this.minZ, sectionY = this.minY;


            Iterator<Entity> currentSectionIterator = nextSection();

            @Override
            protected Entity computeNext() {
                Iterator<Entity> currentSectionIterator = this.currentSectionIterator;
                while (currentSectionIterator != null) {
                    while (currentSectionIterator.hasNext()) {
                        Entity entity = currentSectionIterator.next();
                        if (entity.getBoundingBox().intersects(this.location) && !entity.isSpectator() && entity != this.except) {
                            //skip the dragon piece check without issues by only allowing only EntityClassGroup.NoDragonClassGroup as type
                            return entity;
                        }
                    }
                    this.currentSectionIterator = currentSectionIterator = this.nextSection();
                }
                return this.endOfData();
            }

            private Iterator<Entity> nextSection() {
                while (this.sectionX <= this.maxX) {
                    while (this.sectionZ <= this.maxZ) {
                        while (this.sectionY <= this.maxY) {
                            EntityTrackingSection<Entity> section = this.sectionedEntityCache.findTrackingSection(ChunkSectionPos.asLong(this.sectionX, this.sectionY, this.sectionZ));
                            this.sectionY++;
                            if (section != null) {
                                //noinspection unchecked
                                TypeFilterableList<Entity> allEntities = ((EntityTrackingSectionAccessor<Entity>) section).getCollection();
                                if (!allEntities.isEmpty()) {
                                    //noinspection unchecked
                                    Collection<Entity> entitiesOfType = ((ClassGroupFilterableList<Entity>) allEntities).getAllOfGroupType(this.type);
                                    if (!entitiesOfType.isEmpty()) {
                                        return entitiesOfType.iterator();
                                    }
                                }
                            }
                        }
                        this.sectionZ++;
                        this.sectionY = this.minY;
                    }
                    this.sectionX++;
                    this.sectionZ = this.minZ;
                }

                return null;
            }
        };
    }

    public static <T extends Entity> Iterator<T> iterateEntitiesOfTypeFilter(SectionedEntityCache<Entity> cache, TypeFilter<Entity, T> filter, Box box, Predicate<? super T> predicate, Entity skip) {
        if (filter.getBaseClass().isAssignableFrom(EnderDragonEntity.class)) {
            return iterateEntitiesOfTypeFilterHandleDragon(cache, filter, box, predicate, skip);
        }
        return new AbstractIterator<>() {
            final SectionedEntityCache<Entity> sectionedEntityCache = cache;
            final Box location = box;
            final TypeFilter<Entity, T> type = filter;
            final Predicate<? super T> entityPredicate = predicate;
            final Entity except = skip;

            final int minX = ChunkSectionPos.getSectionCoord(this.location.minX - 2.0D);
            final int minY = ChunkSectionPos.getSectionCoord(this.location.minY - 2.0D);
            final int minZ = ChunkSectionPos.getSectionCoord(this.location.minZ - 2.0D);
            final int maxX = ChunkSectionPos.getSectionCoord(this.location.maxX + 2.0D);
            final int maxY = ChunkSectionPos.getSectionCoord(this.location.maxY + 2.0D);
            final int maxZ = ChunkSectionPos.getSectionCoord(this.location.maxZ + 2.0D);

            int sectionX = this.minX, sectionZ = this.minZ, sectionY = this.minY;


            Iterator<? extends Entity> currentSectionIterator;

            @Override
            protected T computeNext() {
                Iterator<? extends Entity> currentSectionIterator = this.currentSectionIterator;
                TypeFilter<Entity, T> type = this.type;
                while (true) {
                    while (currentSectionIterator != null && currentSectionIterator.hasNext()) {
                        T entity = type.downcast(currentSectionIterator.next());
                        if (entity != null && entity != this.except && entity.getBoundingBox().intersects(this.location) && this.entityPredicate.test(entity)) {
                            //skip the dragon piece check without issues by only allowing only filters without dragon as type
                            return entity;
                        }
                    }
                    this.currentSectionIterator = currentSectionIterator = this.nextSection();
                    if (currentSectionIterator == null) {
                        return this.endOfData();
                    }
                }
            }

            private Iterator<? extends Entity> nextSection() {
                while (this.sectionX <= this.maxX) {
                    while (this.sectionZ <= this.maxZ) {
                        while (this.sectionY <= this.maxY) {
                            EntityTrackingSection<Entity> section = this.sectionedEntityCache.findTrackingSection(ChunkSectionPos.asLong(this.sectionX, this.sectionY, this.sectionZ));
                            this.sectionY++;
                            if (section != null) {
                                //noinspection unchecked
                                TypeFilterableList<Entity> allEntities = ((EntityTrackingSectionAccessor<Entity>) section).getCollection();
                                if (!allEntities.isEmpty()) {
                                    Collection<? extends Entity> entitiesOfType = allEntities.getAllOfType(this.type.getBaseClass());
                                    if (!entitiesOfType.isEmpty()) {
                                        return entitiesOfType.iterator();
                                    }
                                }
                            }
                        }
                        this.sectionZ++;
                        this.sectionY = this.minY;
                    }
                    this.sectionX++;
                    this.sectionZ = this.minZ;
                }

                return null;
            }
        };
    }

    public static <T extends Entity> Iterator<T> iterateEntitiesOfTypeFilterHandleDragon(SectionedEntityCache<Entity> cache, TypeFilter<Entity, T> filter, Box box, Predicate<? super T> predicate, Entity skip) {
        return new AbstractIterator<>() {
            final SectionedEntityCache<Entity> sectionedEntityCache = cache;
            final Box location = box;
            final TypeFilter<Entity, T> type = filter;
            final Predicate<? super T> entityPredicate = predicate;
            final Entity except = skip;
            final int minX = ChunkSectionPos.getSectionCoord(this.location.minX - 2.0D);
            final int minY = ChunkSectionPos.getSectionCoord(this.location.minY - 2.0D);
            final int minZ = ChunkSectionPos.getSectionCoord(this.location.minZ - 2.0D);
            final int maxX = ChunkSectionPos.getSectionCoord(this.location.maxX + 2.0D);
            final int maxY = ChunkSectionPos.getSectionCoord(this.location.maxY + 2.0D);
            final int maxZ = ChunkSectionPos.getSectionCoord(this.location.maxZ + 2.0D);
            EnderDragonPart[] currDragonParts;
            int currPartsIndex;
            int sectionX = this.minX, sectionZ = this.minZ, sectionY = this.minY;


            Iterator<? extends Entity> currentSectionIterator;

            @Override
            protected T computeNext() {
                Iterator<? extends Entity> currentSectionIterator = this.currentSectionIterator;
                TypeFilter<Entity, T> type = this.type;
                while (true) {
                    while (this.currDragonParts != null) {
                        EnderDragonPart enderDragonPart = this.currDragonParts[this.currPartsIndex++];
                        if (this.currPartsIndex >= this.currDragonParts.length) {
                            this.currDragonParts = null;
                        }
                        T entity = type.downcast(enderDragonPart);
                        if (entity != null && entity != this.except && this.entityPredicate.test(entity)) {
                            return entity;
                        }
                    }
                    while (currentSectionIterator != null && currentSectionIterator.hasNext()) {
                        T entity = type.downcast(currentSectionIterator.next());
                        if (entity != null && entity != this.except && entity.getBoundingBox().intersects(this.location) && this.entityPredicate.test(entity)) {
                            //handle the dragon pieces in the same order as vanilla
                            if (entity instanceof EnderDragonEntity) {
                                this.currDragonParts = ((EnderDragonEntity) entity).getBodyParts();
                                this.currPartsIndex = 0;
                            }
                            return entity;
                        }
                    }
                    this.currentSectionIterator = currentSectionIterator = this.nextSection();
                    if (currentSectionIterator == null) {
                        return this.endOfData();
                    }
                }
            }

            private Iterator<? extends Entity> nextSection() {
                while (this.sectionX <= this.maxX) {
                    while (this.sectionZ <= this.maxZ) {
                        while (this.sectionY <= this.maxY) {
                            EntityTrackingSection<Entity> section = this.sectionedEntityCache.findTrackingSection(ChunkSectionPos.asLong(this.sectionX, this.sectionY, this.sectionZ));
                            this.sectionY++;
                            if (section != null) {
                                //noinspection unchecked
                                TypeFilterableList<Entity> allEntities = ((EntityTrackingSectionAccessor<Entity>) section).getCollection();
                                if (!allEntities.isEmpty()) {
                                    Collection<? extends Entity> entitiesOfType = allEntities.getAllOfType(this.type.getBaseClass());
                                    if (!entitiesOfType.isEmpty()) {
                                        return entitiesOfType.iterator();
                                    }
                                }
                            }
                        }
                        this.sectionZ++;
                        this.sectionY = this.minY;
                    }
                    this.sectionX++;
                    this.sectionZ = this.minZ;
                }

                return null;
            }
        };
    }


    public static boolean areNeighborsWithinSameChunk(BlockPos pos) {
        int localX = pos.getX() & 15;
        int localY = pos.getY() & 15;
        int localZ = pos.getZ() & 15;

        return localX > 0 && localY > 0 && localZ > 0 && localX < 15 && localY < 15 && localZ < 15;
    }
}
