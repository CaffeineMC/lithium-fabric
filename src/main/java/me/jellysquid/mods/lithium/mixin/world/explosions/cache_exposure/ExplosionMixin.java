package me.jellysquid.mods.lithium.mixin.world.explosions.cache_exposure;

import me.jellysquid.mods.lithium.common.world.ExplosionCache;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Optimizations for Explosions: Remove duplicate {@link Explosion#getExposure(Vec3d, Entity)} calls.
 * @author Crosby
 */
@Mixin(Explosion.class)
public abstract class ExplosionMixin implements ExplosionCache {
    @Unique private float cachedExposure;
    @Unique private Entity cachedEntity;

    @Override
    public void lithium_fabric$cacheExposure(Entity entity, float exposure) {
        this.cachedExposure = exposure;
        this.cachedEntity = entity;
    }

    @Redirect(method = "collectBlocksAndDamageEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/explosion/Explosion;getExposure(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/entity/Entity;)F"))
    private float returnCachedExposure(Vec3d source, Entity entity) {
        return this.cachedEntity == entity ? this.cachedExposure : Explosion.getExposure(source, entity);
    }
}
