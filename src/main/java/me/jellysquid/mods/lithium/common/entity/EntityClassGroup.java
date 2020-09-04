package me.jellysquid.mods.lithium.common.entity;

import it.unimi.dsi.fastutil.objects.Reference2ByteOpenHashMap;
import net.minecraft.entity.Entity;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Class for grouping Entity classes that meet some requirement for use in TypeFilterableList
 * Designed to allow create groups of entity classes that are updated when mods add new entities that fit into the group.
 * @author 2No2Name
 */
public class EntityClassGroup {
    public static final EntityClassGroup COLLISION_BOX_OVERRIDE = new EntityClassGroup(
            (Class<?> entityClass) -> isMethodOverriden(entityClass, Entity.class, "method_30948"));
    public static final EntityClassGroup HARD_COLLISION_BOX_OVERRIDE = new EntityClassGroup(
            (Class<?> entityClass) -> isMethodOverriden(entityClass, Entity.class, "method_30949", Entity.class));

    private final Predicate<Class<?>> classFitEvaluator;
    private volatile Reference2ByteOpenHashMap<Class<?>> class2GroupContains;

    public EntityClassGroup(Predicate<Class<?>> classFitEvaluator) {
        this.class2GroupContains = new Reference2ByteOpenHashMap<>();
        Objects.requireNonNull(classFitEvaluator);
        this.classFitEvaluator = classFitEvaluator;
    }

    public boolean contains(Class<?> entityClass) {
        byte contains = this.class2GroupContains.getOrDefault(entityClass, (byte)2);
        if (contains != 2) {
            return contains == 1;
        } else {
            //synchronizing here to avoid multiple threads replacing the map at the same time, and therefore possibly undoing progress
            //it could also be fixed by using an AtomicReference's CAS, but we are writing very rarely (less than 150 for the total runtime in vanilla)
            synchronized (this) {
                contains = this.class2GroupContains.getOrDefault(entityClass, (byte)2);
                if (contains != 2) {
                    return contains == 1;
                }
                //construct new map to avoid thread safety problems.
                //the map we publish in the volatile field is effectively immutable to avoid thread safety issues when modifying the map
                //the overhead of constructing the new map and storing in volatile is negligible, because there is only a limited amount of classes to evaluate
                Reference2ByteOpenHashMap<Class<?>> newMap = new Reference2ByteOpenHashMap<>(this.class2GroupContains);
                newMap.put(entityClass, this.classFitEvaluator.test(entityClass) ? (byte)1 : (byte)0);
                this.class2GroupContains = newMap;
            }
            return this.class2GroupContains.getOrDefault(entityClass, (byte)2) == 1;
        }
    }

    public static boolean isMethodOverriden(Class<?> clazz, Class<?> superclass, String methodName, Class<?>... methodArgs) {
        while(clazz != null && clazz != superclass && superclass.isAssignableFrom(clazz)) {
            try {
                clazz.getDeclaredMethod(methodName, methodArgs);
                return true;
            } catch (NoSuchMethodException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return false;
    }
}