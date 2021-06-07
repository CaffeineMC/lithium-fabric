package me.jellysquid.mods.lithium.common.entity.tracker.nearby;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceArrayMap;
import me.jellysquid.mods.lithium.common.util.tuples.WorldSectionBox;
import me.jellysquid.mods.lithium.mixin.block.hopper.EntityTrackingSectionAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.util.collection.TypeFilterableList;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

public class SectionedInventoryEntityMovementTracker<S> extends SectionedEntityMovementTracker<Entity, S> {
    private static final WeakHashMap<World, Reference2ReferenceArrayMap<Class<?>, WeakHashMap<WorldSectionBox, SectionedInventoryEntityMovementTracker<?>>>> trackersByPositionByClassByWorld = new WeakHashMap<>();

    public SectionedInventoryEntityMovementTracker(WorldSectionBox entityAccessBox, Class<S> clazz) {
        super(entityAccessBox, clazz);
    }

    public static <S> SectionedInventoryEntityMovementTracker<S> getOrCreate(World world, Box interactionArea, Class<S> clazz) {
        WorldSectionBox worldSectionBox = WorldSectionBox.entityAccessBox(world, interactionArea);
        Reference2ReferenceArrayMap<Class<?>, WeakHashMap<WorldSectionBox, SectionedInventoryEntityMovementTracker<?>>> trackersByPositionByClass = trackersByPositionByClassByWorld.computeIfAbsent(world, world1 -> new Reference2ReferenceArrayMap<>());
        WeakHashMap<WorldSectionBox, SectionedInventoryEntityMovementTracker<?>> trackersByPosition = trackersByPositionByClass.computeIfAbsent(clazz, aClass -> new WeakHashMap<>(1));
        //noinspection unchecked
        SectionedInventoryEntityMovementTracker<S> tracker = (SectionedInventoryEntityMovementTracker<S>) trackersByPosition.get(worldSectionBox);
        if (tracker == null) {
            tracker = new SectionedInventoryEntityMovementTracker<>(worldSectionBox, clazz);
            trackersByPosition.put(worldSectionBox, tracker);
        }
        return tracker;
    }

    public List<S> getEntities(Box box) {
        ArrayList<S> entities = new ArrayList<>();
        for (int i = 0; i < this.sortedSections.size(); i++) {
            if (this.sectionVisible[i]) {
                //noinspection unchecked
                TypeFilterableList<S> collection = ((EntityTrackingSectionAccessor<S>) this.sortedSections.get(i)).getCollection();

                for (S entity : collection.getAllOfType(this.clazz)) {
                    Entity inventoryEntity = (Entity) entity;
                    if (inventoryEntity.isAlive() && inventoryEntity.getBoundingBox().intersects(box)) {
                        entities.add(entity);
                    }
                }
            }
        }
        return entities;
    }
}
