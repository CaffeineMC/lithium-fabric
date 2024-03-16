package me.jellysquid.mods.lithium.mixin.world.explosions.cache_exposure;

import me.jellysquid.mods.lithium.common.world.ExplosionCache;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Optimizations for Explosions: Remove duplicate {@link Explosion#getExposure(Vec3d, Entity)} calls.
 * @author Crosby
 */
@Mixin(Explosion.class)
public abstract class ExplosionMixin implements ExplosionCache {
    @Unique private float cachedExposure;
    @Unique private Entity cachedEntity;

    @Override
    public float lithium_fabric$getCachedExposure() {
        return this.cachedExposure;
    }

    @Override
    public Entity lithium_fabric$getCachedEntity() {
        return this.cachedEntity;
    }

    @Shadow
    public static float getExposure(Vec3d source, Entity entity) {
        throw new AssertionError();
    }

    /**
     * Since {@link Explosion#getExposure(Vec3d, Entity)} may called either once or twice with the same parameters, we
     * calculate the value pre-emptively and redirect the other calls.
     * @author Crosby
     */
    @SuppressWarnings("InvalidInjectorMethodSignature") // signature is valid
    @Inject(method = "collectBlocksAndDamageEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/explosion/ExplosionBehavior;shouldDamage(Lnet/minecraft/world/explosion/Explosion;Lnet/minecraft/entity/Entity;)Z"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void preCalculateExposure(CallbackInfo ci, Set<?> set, int i, float q, int k, int l, int r, int s, int t, int u, List<?> list, Vec3d vec3d, Iterator<?> var12, Entity entity) {
        this.cachedExposure = getExposure(vec3d, entity);
        this.cachedEntity = entity;
    }

    @Redirect(method = "collectBlocksAndDamageEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/explosion/Explosion;getExposure(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/entity/Entity;)F"))
    private float returnCachedExposure(Vec3d source, Entity entity) {
        return this.cachedExposure;
    }
}
