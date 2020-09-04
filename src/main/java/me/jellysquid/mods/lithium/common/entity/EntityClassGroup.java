package me.jellysquid.mods.lithium.common.entity;

import it.unimi.dsi.fastutil.objects.Reference2ByteOpenHashMap;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.vehicle.MinecartEntity;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Class for grouping Entity classes that meet some requirement for use in TypeFilterableList
 * Designed to allow create groups of entity classes that are updated when mods add new entities that fit into the group.
 * @author 2No2Name
 */
public class EntityClassGroup {
    public static final EntityClassGroup COLLISION_BOX_OVERRIDE = new EntityClassGroup(
            (Class<?> entityClass) -> isMethodDefinedInSubclass(entityClass, Entity.class, FabricLoader.getInstance().getMappingResolver().mapMethodName("intermediary", "net.minecraft.class_1297", "method_30948", "()Z")));
    public static final EntityClassGroup HARD_COLLISION_BOX_OVERRIDE = new EntityClassGroup(
            (Class<?> entityClass) -> isMethodDefinedInSubclass(entityClass, Entity.class, FabricLoader.getInstance().getMappingResolver().mapMethodName("intermediary", "net.minecraft.class_1297", "method_30949", "(Lnet/minecraft/class_1297;)Z"), Entity.class));
    static {
        //sanity check: in case intermediary mappings changed, we fail
        if ((!HARD_COLLISION_BOX_OVERRIDE.contains(MinecartEntity.class))) {
            throw new AssertionError();
        }
        if ((!COLLISION_BOX_OVERRIDE.contains(ShulkerEntity.class))) {
            throw new AssertionError();
        }
        if ((HARD_COLLISION_BOX_OVERRIDE.contains(ShulkerEntity.class))) {
            throw new AssertionError();
        }
        COLLISION_BOX_OVERRIDE.clear();
        HARD_COLLISION_BOX_OVERRIDE.clear();
    }


    private final Predicate<Class<?>> classFitEvaluator;
    private volatile Reference2ByteOpenHashMap<Class<?>> class2GroupContains;

    public EntityClassGroup(Predicate<Class<?>> classFitEvaluator) {
        this.class2GroupContains = new Reference2ByteOpenHashMap<>();
        Objects.requireNonNull(classFitEvaluator);
        this.classFitEvaluator = classFitEvaluator;
    }

    public void clear() {
        this.class2GroupContains = new Reference2ByteOpenHashMap<>();
    }

    public boolean contains(Class<?> entityClass) {
        byte contains = this.class2GroupContains.getOrDefault(entityClass, (byte)2);
        if (contains != 2) {
            return contains == 1;
        } else {
           return testAndAddClass(entityClass);
        }
    }

    private boolean testAndAddClass(Class<?> entityClass) {
        byte contains;
        //synchronizing here to avoid multiple threads replacing the map at the same time, and therefore possibly undoing progress
        //it could also be fixed by using an AtomicReference's CAS, but we are writing very rarely (less than 150 times for the total game runtime in vanilla)
        synchronized (this) {
            //test the same condition again after synchronizing, as the collection might have been updated while this thread blocked
            contains = this.class2GroupContains.getOrDefault(entityClass, (byte)2);
            if (contains != 2) {
                return contains == 1;
            }
            //construct new map instead of updating the old map to avoid thread safety problems
            //the map is not modified after publication
            Reference2ByteOpenHashMap<Class<?>> newMap = this.class2GroupContains.clone();
            contains = this.classFitEvaluator.test(entityClass) ? (byte)1 : (byte)0;
            newMap.put(entityClass, contains);
            //publish the new map in a volatile field, so that all threads reading after this write can also see all changes to the map done before the write
            this.class2GroupContains = newMap;
        }
        return contains == 1;
    }

    public static boolean isMethodDefinedInSubclass(Class<?> clazz, Class<?> superclass, String methodName, Class<?>... methodArgs) {
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