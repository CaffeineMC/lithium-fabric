package me.jellysquid.mods.lithium.common.util.math;

import net.minecraft.util.math.Vec3d;

public class MutVec3d {
    public double x, y, z;

    public void add(Vec3d vec, double factor) {
        this.x += (vec.x * factor);
        this.y += (vec.y * factor);
        this.z += (vec.z * factor);
    }

    public Vec3d toImmutable() {
        return new Vec3d(this.x, this.y, this.z);
    }

    public double lengthSquared() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }
}
