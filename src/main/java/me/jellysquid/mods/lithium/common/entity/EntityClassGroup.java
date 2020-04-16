package me.jellysquid.mods.lithium.common.entity;

import net.minecraft.entity.Entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Class for grouping Entity classes that meet some requirement for use in TypeFilterableList
 * @author 2No2Name
 */
public class EntityClassGroup {
    //Keep a set of classes we know, so we only evaluate them once.
    private static final ConcurrentHashMap<Class<?>, /*Unused Value*/Object> knownEntityClasses = new ConcurrentHashMap<>();
    //Keep track of available class groups for updating them in case an entity class is instantiated for the first time
    private static final List<EntityClassGroup> entityClassGroups = new ArrayList<>();

    public static final EntityClassGroup COLLISION_BOX_OVERRIDE = new EntityClassGroup(
            (Class<?> entityClass) -> {
                boolean overwritten;
                while(entityClass != null && entityClass != Entity.class) {
                    try {
                        overwritten = true;
                        entityClass.getDeclaredMethod("getCollisionBox");
                    } catch (NoSuchMethodException e) {
                        overwritten = false;
                        entityClass = entityClass.getSuperclass();
                    }
                    if (overwritten) {
                        return true;
                    }
                }
                return false;
            }
    );
    public static final EntityClassGroup HARD_COLLISION_BOX_OVERRIDE = new EntityClassGroup(
        (Class<?> entityClass) -> {
            boolean overwritten;
            while(entityClass != null && entityClass != Entity.class) {
                try {
                    overwritten = true;
                    entityClass.getDeclaredMethod("getHardCollisionBox", Entity.class);
                } catch (NoSuchMethodException e) {
                    overwritten = false;
                    entityClass = entityClass.getSuperclass();
                }
                if (overwritten)
                    return true;
            }
            return false;
        }
    );



    private final ConcurrentHashMap<Class<?>, /*Unused Value*/Object> classGroup;
    private final Function<Class<?>, Boolean> classFitEvaluator;

    public EntityClassGroup() {
        this.classGroup = new ConcurrentHashMap<>();
        EntityClassGroup.entityClassGroups.add(this);
        this.classFitEvaluator = null;

    }
    public EntityClassGroup(Function<Class<?>, Boolean> classFitEvaluator) {
        this.classGroup = new ConcurrentHashMap<>();
        EntityClassGroup.entityClassGroups.add(this);
        this.classFitEvaluator = classFitEvaluator;
    }
    public EntityClassGroup(Collection<Class<? >> classGroupCollection, Function<Class<?>, Boolean> classFitEvaluator) {
        this(classFitEvaluator);
        for (Class<?> cl : classGroupCollection) {
            this.classGroup.put(cl, cl);
        }
    }

    public EntityClassGroup add(Class<?> entityClass) {
        this.classGroup.put(entityClass, entityClass);
        return this;
    }

    public boolean contains(Class<?> entityClass) {
        EntityClassGroup.analyseEntityClass(entityClass);
        return this.classGroup.containsKey(entityClass);
    }

    public Collection<Class<?>> getCollection() {
        return this.classGroup.keySet();
    }

    public void addClassIfFitting(Class<?> discoveredEntityClass) {
        if (this.classGroup.containsKey(discoveredEntityClass)) {
            return;
        }
        if (classFitEvaluator != null && classFitEvaluator.apply(discoveredEntityClass)) {
            this.classGroup.put(discoveredEntityClass, discoveredEntityClass);
        }
    }

    public static void analyseEntityClass(Class<?> entityClass) {
        if (EntityClassGroup.knownEntityClasses.containsKey(entityClass)) {
            return;
        }
        EntityClassGroup.knownEntityClasses.put(entityClass, entityClass);

        for (EntityClassGroup entityClassGroup : EntityClassGroup.entityClassGroups) {
            entityClassGroup.addClassIfFitting(entityClass);
        }
    }
}