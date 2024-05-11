package me.jellysquid.mods.lithium.mixin.world.explosions.cache_exposure;

import me.jellysquid.mods.lithium.common.world.ExplosionCache;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Optimizations for Explosions: Remove duplicate {@link Explosion#getExposure(Vec3d, Entity)} calls.
 * @author Crosby
 */
@Mixin(ExplosionBehavior.class)
public class ExplosionBehaviorMixin {
    @Unique private ExplosionCache explosion;

    @Inject(method = "calculateDamage", at = @At("HEAD"))
    private void captureExplosion(Explosion explosion, Entity entity, CallbackInfoReturnable<Float> cir) {
        this.explosion = (ExplosionCache) explosion;
    }

    /**
     * Try to use the exposure value pre-calculated in {@link Explosion#collectBlocksAndDamageEntities()}.
     * Check entity equality to prevent undefined behaviour caused by calling
     * {@link ExplosionBehavior#calculateDamage(Explosion, Entity)} from outside of
     * {@link Explosion#collectBlocksAndDamageEntities()}.
     * @author Crosby
     */
    @Redirect(method = "calculateDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/explosion/Explosion;getExposure(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/entity/Entity;)F"))
    private float useCachedExposure(Vec3d source, Entity entity) {
        float exposure = Explosion.getExposure(source, entity);
        this.explosion.lithium_fabric$cacheExposure(entity, exposure);
        return exposure;
    }
}
