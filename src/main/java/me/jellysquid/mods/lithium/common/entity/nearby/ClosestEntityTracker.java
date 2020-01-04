package me.jellysquid.mods.lithium.common.entity.nearby;

import net.minecraft.entity.LivingEntity;

import java.util.HashSet;
import java.util.Set;

public class ClosestEntityTracker<T extends LivingEntity> implements NearbyEntityListener {
    private final Class<T> clazz;
    private final LivingEntity self;

    private final int range;

    private final Set<T> nearby = new HashSet<>();

    public ClosestEntityTracker(Class<T> clazz, LivingEntity self, int range) {
        this.clazz = clazz;
        this.self = self;
        this.range = range;
    }

    @Override
    public int getRange() {
        return this.range;
    }

    @Override
    public void onEntityEnteredRange(LivingEntity entity) {
        if (!this.clazz.isInstance(entity)) {
            return;
        }

        this.nearby.add(this.clazz.cast(entity));
    }

    @Override
    public void onEntityLeftRange(LivingEntity entity) {
        if (!this.clazz.isInstance(entity)) {
            return;
        }

        this.nearby.remove(this.clazz.cast(entity));
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

        if (nearestDistance <= this.range) {
            return nearest;
        }

        return null;
    }
}
