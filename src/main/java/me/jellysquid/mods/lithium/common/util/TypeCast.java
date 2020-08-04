package me.jellysquid.mods.lithium.common.util;

import net.minecraft.world.explosion.Explosion;

/**
 * Class to work around being unable to cast SomeClassMixin to SomeClass inside SomeClassMixin.
 * Add methods for ohter types when needed.
 *
 * Hopefully(!) the casts can be inlined and completely eliminated (casting SomeClass to SomeClass never doing anything).
 *
 * @author 2No2Name
 */
public class TypeCast {
    public static Explosion toExplosion(Object returnValue) {
        return (Explosion) returnValue;
    }
}
