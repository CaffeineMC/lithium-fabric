package me.jellysquid.mods.lithium.mixin.world.explosions.fast_exposure;

import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * Optimizations for Explosions: Reduce workload for exposure calculations
 * @author Crosby
 */
@Mixin(Explosion.class)
public class ExplosionMixin {
    @Unique private static final RaycastContext EMPTY = new RaycastContext(null, null, null, null, (ShapeContext) null);
    @Unique private static final BlockHitResult MISS = BlockHitResult.createMissed(null, null, null);

    @Unique private static Vec3d explosionSource;
    @Unique private static Vec3d raycastTarget;

    /**
     * Skip exposure calculations (used for calculating velocity) on entities which get discarded when blown up. (For
     * example: dropped items & experience orbs)
     * This doesn't improve performance when {@code world.explosions.cache_exposure} is active, but it doesn't hurt to
     * keep.
     * @author Crosby
     */
    @Inject(method = "getExposure", at = @At("HEAD"), cancellable = true)
    private static void skipDeadEntities(Vec3d source, Entity entity, CallbackInfoReturnable<Float> cir) {
        Entity.RemovalReason removalReason = entity.getRemovalReason();
        if (removalReason != null && removalReason.shouldDestroy()) cir.setReturnValue(0f);
    }

    /**
     * Since the {@code NEW} injection point doesn't allow a return value of {@code null}, we return a constant, empty
     * {@link RaycastContext} instead to prevent a useless object allocation.
     * @author Crosby
     */
    @Redirect(method = "getExposure", at = @At(value = "NEW", target = "Lnet/minecraft/world/RaycastContext;"))
    private static RaycastContext removeAllocation(Vec3d start, Vec3d end, RaycastContext.ShapeType shapeType, RaycastContext.FluidHandling fluidHandling, Entity entity) {
        return EMPTY;
    }

    @Inject(method = "getExposure", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;raycast(Lnet/minecraft/world/RaycastContext;)Lnet/minecraft/util/hit/BlockHitResult;"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private static void captureLocals(Vec3d source, Entity entity, CallbackInfoReturnable<Float> cir, Box box, double d,
                                      double e, double f, double g, double h, int i, int j, double k, double l,
                                      double m, double n, double o, double p, Vec3d target) {
        explosionSource = source;
        raycastTarget = target;
    }

    @Redirect(method = "getExposure", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;raycast(Lnet/minecraft/world/RaycastContext;)Lnet/minecraft/util/hit/BlockHitResult;"))
    private static BlockHitResult optimizeRaycast(World instance, RaycastContext raycastContext) {
        return simpleRaycast(instance, raycastTarget, explosionSource);
    }

    /**
     * Since we don't care about fluid handling, hit direction, and all that other fluff, we can massively simplify the
     * work done in the hit factory and make the miss factory return an empty constant.
     * @author Crosby
     */
    @Unique
    private static BlockHitResult simpleRaycast(World world, Vec3d source, Vec3d target) {
        return BlockView.raycast(source, target, null, (_null, blockPos) -> {
            BlockState blockState = world.getBlockState(blockPos);
            return blockState.getCollisionShape(world, blockPos).raycast(source, target, blockPos);
        }, _null -> MISS);
    }
}
