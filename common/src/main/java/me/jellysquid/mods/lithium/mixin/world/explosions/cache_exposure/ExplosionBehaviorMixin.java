package me.jellysquid.mods.lithium.mixin.world.explosions.cache_exposure;

import me.jellysquid.mods.lithium.common.world.ExplosionCache;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Optimizations for Explosions: Remove duplicate {@link Explosion#getSeenPercent(Vec3, Entity)} calls.
 * @author Crosby
 */
@Mixin(ExplosionDamageCalculator.class)
public class ExplosionBehaviorMixin {
    @Unique private ExplosionCache explosion;

    @Inject(method = "getEntityDamageAmount", at = @At("HEAD"))
    private void captureExplosion(Explosion explosion, Entity entity, CallbackInfoReturnable<Float> cir) {
        this.explosion = (ExplosionCache) explosion;
    }

    /**
     * Try to use the exposure value pre-calculated in {@link Explosion#explode()}.
     * Check entity equality to prevent undefined behaviour caused by calling
     * {@link ExplosionDamageCalculator#getEntityDamageAmount(Explosion, Entity)} from outside of
     * {@link Explosion#explode()}.
     * @author Crosby
     */
    @Redirect(method = "getEntityDamageAmount", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Explosion;getSeenPercent(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/entity/Entity;)F"))
    private float useCachedExposure(Vec3 source, Entity entity) {
        float exposure = Explosion.getSeenPercent(source, entity);
        this.explosion.lithium_fabric$cacheExposure(entity, exposure);
        return exposure;
    }
}
