package net.caffeinemc.mods.lithium.common.entity;

import it.unimi.dsi.fastutil.objects.Reference2ByteOpenHashMap;
import net.caffeinemc.mods.lithium.common.reflection.ReflectionUtil;
import net.caffeinemc.mods.lithium.common.services.PlatformMappingInformation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.projectile.windcharge.BreezeWindCharge;
import net.minecraft.world.entity.projectile.windcharge.WindCharge;
import net.minecraft.world.entity.vehicle.Minecart;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.logging.Logger;

/**
 * Class for grouping Entity classes by some property for use in TypeFilterableList
 * It is intended that an EntityClassGroup acts as if it was immutable, however we cannot predict which subclasses of
 * Entity might appear. Therefore we evaluate whether a class belongs to the class group when it is first seen.
 * Once a class was evaluated the result of it is cached and cannot be changed.
 *
 * @author 2No2Name
 */
public class EntityClassGroup {
    public static final EntityClassGroup CUSTOM_COLLIDE_LIKE_MINECART_BOAT_WINDCHARGE; //aka entities that will attempt to collide with all other entities when moving

    static {
        String remapped_collidesWith = PlatformMappingInformation.INSTANCE.mapMethodName("intermediary", "net.minecraft.class_1297", "method_30949", "(Lnet/minecraft/class_1297;)Z", "canCollideWith");
        CUSTOM_COLLIDE_LIKE_MINECART_BOAT_WINDCHARGE = new EntityClassGroup(
                (Class<?> entityClass) -> ReflectionUtil.hasMethodOverride(entityClass, Entity.class, true, remapped_collidesWith, Entity.class));

        //sanity check: in case intermediary mappings changed, we fail
        if ((!CUSTOM_COLLIDE_LIKE_MINECART_BOAT_WINDCHARGE.contains(Minecart.class))) {
            throw new AssertionError();
        }
        if ((!CUSTOM_COLLIDE_LIKE_MINECART_BOAT_WINDCHARGE.contains(WindCharge.class)) || (!CUSTOM_COLLIDE_LIKE_MINECART_BOAT_WINDCHARGE.contains(BreezeWindCharge.class))) {
            throw new AssertionError();
        }
        if ((CUSTOM_COLLIDE_LIKE_MINECART_BOAT_WINDCHARGE.contains(Shulker.class))) {
            //should not throw an Error here, because another mod *could* add the method to ShulkerEntity. Wwarning when this sanity check fails.
            Logger.getLogger("Lithium EntityClassGroup").warning("Either Lithium EntityClassGroup is broken or something else gave Shulkers the minecart-like collision behavior.");
        }
        CUSTOM_COLLIDE_LIKE_MINECART_BOAT_WINDCHARGE.clear();
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
        byte contains = this.class2GroupContains.getOrDefault(entityClass, (byte) 2);
        if (contains != 2) {
            return contains == 1;
        } else {
            return this.testAndAddClass(entityClass);
        }
    }

    boolean testAndAddClass(Class<?> entityClass) {
        byte contains;
        //synchronizing here to avoid multiple threads replacing the map at the same time, and therefore possibly undoing progress
        //it could also be fixed by using an AtomicReference's CAS, but we are writing very rarely (less than 150 times for the total game runtime in vanilla)
        synchronized (this) {
            //test the same condition again after synchronizing, as the collection might have been updated while this thread blocked
            contains = this.class2GroupContains.getOrDefault(entityClass, (byte) 2);
            if (contains != 2) {
                return contains == 1;
            }
            //construct new map instead of updating the old map to avoid thread safety problems
            //the map is not modified after publication
            Reference2ByteOpenHashMap<Class<?>> newMap = this.class2GroupContains.clone();
            contains = this.classFitEvaluator.test(entityClass) ? (byte) 1 : (byte) 0;
            newMap.put(entityClass, contains);
            //publish the new map in a volatile field, so that all threads reading after this write can also see all changes to the map done before the write
            this.class2GroupContains = newMap;
        }
        return contains == 1;
    }

    public static class NoDragonClassGroup extends EntityClassGroup {
        public static final NoDragonClassGroup BOAT_SHULKER_LIKE_COLLISION; //aka entities that other entities will do block-like collisions with when moving

        static {
            String remapped_isCollidable = PlatformMappingInformation.INSTANCE.mapMethodName("intermediary", "net.minecraft.class_1297", "method_30948", "()Z", "canBeCollidedWith");
            BOAT_SHULKER_LIKE_COLLISION = new NoDragonClassGroup(
                    (Class<?> entityClass) -> ReflectionUtil.hasMethodOverride(entityClass, Entity.class, true, remapped_isCollidable));

            if ((!BOAT_SHULKER_LIKE_COLLISION.contains(Shulker.class))) {
                throw new AssertionError();
            }
            BOAT_SHULKER_LIKE_COLLISION.clear();
        }

        public NoDragonClassGroup(Predicate<Class<?>> classFitEvaluator) {
            super(classFitEvaluator);
            if (classFitEvaluator.test(EnderDragon.class)) {
                throw new IllegalArgumentException("EntityClassGroup.NoDragonClassGroup cannot be initialized: Must exclude EnderDragonEntity!");
            }
        }
    }
}