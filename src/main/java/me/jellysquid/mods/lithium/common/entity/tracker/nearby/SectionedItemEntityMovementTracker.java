package me.jellysquid.mods.lithium.common.entity.tracker.nearby;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceArrayMap;
import me.jellysquid.mods.lithium.common.util.collections.BucketedList;
import me.jellysquid.mods.lithium.common.util.tuples.WorldSectionBox;
import me.jellysquid.mods.lithium.mixin.block.hopper.EntityTrackingSectionAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.util.collection.TypeFilterableList;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;
import java.util.WeakHashMap;

public class SectionedItemEntityMovementTracker<S extends Entity> extends SectionedEntityMovementTracker<Entity, S> {
    private static final WeakHashMap<World, Reference2ReferenceArrayMap<Class<?>, WeakHashMap<WorldSectionBox, SectionedItemEntityMovementTracker<?>>>> trackersByPositionByClassByWorld = new WeakHashMap<>();

    public SectionedItemEntityMovementTracker(WorldSectionBox worldSectionBox, Class<S> clazz) {
        super(worldSectionBox, clazz);
    }

    public static <S extends Entity> SectionedItemEntityMovementTracker<S> getOrCreate(World world, Box encompassingBox, Class<S> clazz) {
        WorldSectionBox worldSectionBox = WorldSectionBox.entityAccessBox(world, encompassingBox);
        Reference2ReferenceArrayMap<Class<?>, WeakHashMap<WorldSectionBox, SectionedItemEntityMovementTracker<?>>> trackersByPositionByClass = trackersByPositionByClassByWorld.computeIfAbsent(world, world1 -> new Reference2ReferenceArrayMap<>());
        WeakHashMap<WorldSectionBox, SectionedItemEntityMovementTracker<?>> trackersByPosition = trackersByPositionByClass.computeIfAbsent(clazz, aClass -> new WeakHashMap<>(1));
        //noinspection unchecked
        SectionedItemEntityMovementTracker<S> tracker = (SectionedItemEntityMovementTracker<S>) trackersByPosition.get(worldSectionBox);
        if (tracker == null) {
            tracker = new SectionedItemEntityMovementTracker<>(worldSectionBox, clazz);
            trackersByPosition.put(worldSectionBox, tracker);
        }
        return tracker;
    }

    public List<S> getEntities(Box[] areas) {
        int numBoxes = areas.length - 1;
        BucketedList<S> entities = new BucketedList<>(numBoxes);
        Box encompassingBox = areas[numBoxes];
        for (int sectionIndex = 0; sectionIndex < this.sortedSections.size(); sectionIndex++) {
            if (this.sectionVisible[sectionIndex]) {
                //noinspection unchecked
                TypeFilterableList<S> collection = ((EntityTrackingSectionAccessor<S>) this.sortedSections.get(sectionIndex)).getCollection();

                for (S entity : collection.getAllOfType(this.clazz)) {
                    if (entity.isAlive()) {
                        Box entityBoundingBox = entity.getBoundingBox();
                        //even though there are usually only two boxes to check, checking the encompassing box only will be faster in most cases
                        //In vanilla the number of boxes checked is always 2. Here it is 1 (miss) and 2-3 (hit)
                        if (entityBoundingBox.intersects(encompassingBox)) {
                            for (int j = 0; j < numBoxes; j++) {
                                if (entityBoundingBox.intersects(areas[j])) {
                                    entities.addToBucket(j, entity);
                                }
                            }
                        }
                    }
                }
            }
        }
        return entities;
    }
}
