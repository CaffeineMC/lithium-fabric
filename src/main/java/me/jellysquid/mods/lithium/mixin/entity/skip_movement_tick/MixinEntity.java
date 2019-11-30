package me.jellysquid.mods.lithium.mixin.entity.skip_movement_tick;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class MixinEntity {
    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    private void checkMove(MovementType type, Vec3d vec, CallbackInfo ci) {
        // Check if the entity is actually going to move any meaningful distance. If not,
        // we can avoid expensive collision checking code. We only permit this optimization
        // if the entity is moving itself as there may be some special edge cases with pistons.
        if (type == MovementType.SELF && vec.lengthSquared() < 0.0001D) {
            ci.cancel();
        }
    }
}
