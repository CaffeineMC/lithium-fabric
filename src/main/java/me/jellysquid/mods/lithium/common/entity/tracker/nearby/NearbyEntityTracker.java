package me.jellysquid.mods.lithium.common.entity.tracker.nearby;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

import java.util.HashSet;
import java.util.Set;

/**
 * Maintains a collection of all entities within the range of this listener. This allows AI goals to quickly
 * assess nearby entities which match the provided class.
 */
public class NearbyEntityTracker<T extends LivingEntity> implements NearbyEntityListener {
    private final Class<T> clazz;
    private final LivingEntity self;

    private final int rangeC;
    private final float rangeSq;

    private final Set<T> nearby = new HashSet<>();

    public NearbyEntityTracker(Class<T> clazz, LivingEntity self, float range) {
        this.clazz = clazz;
        this.self = self;
        this.rangeSq = range * range;
        this.rangeC = MathHelper.roundUp(MathHelper.ceil(range), 16) >> 4;
    }

    @Override
    public int getChunkRange() {
        return this.rangeC;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onEntityEnteredRange(LivingEntity entity) {
        if (!this.clazz.isInstance(entity)) {
            return;
        }

        this.nearby.add((T) entity);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onEntityLeftRange(LivingEntity entity) {
        if (this.nearby.isEmpty() || !this.clazz.isInstance(entity)) {
            return;
        }

        this.nearby.remove((T) entity);
    }

    public T getClosestEntity() {
        double x = this.self.getX();
        double y = this.self.getY();
        double z = this.self.getZ();

        T nearest = null;
        double nearestDistance = Double.POSITIVE_INFINITY;

        for (T entity : this.nearby) {
            double distance = entity.squaredDistanceTo(x, y, z);

            if (distance < nearestDistance) {
                nearest = entity;
                nearestDistance = distance;
            }
        }

        if (nearestDistance <= this.rangeSq) {
            return nearest;
        }

        return null;
    }
}
