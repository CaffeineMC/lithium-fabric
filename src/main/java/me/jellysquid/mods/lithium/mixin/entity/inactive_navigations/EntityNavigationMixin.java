package me.jellysquid.mods.lithium.mixin.entity.inactive_navigations;

import me.jellysquid.mods.lithium.common.entity.NavigatingEntity;
import me.jellysquid.mods.lithium.common.world.ServerWorldExtended;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PathNavigation.class)
public abstract class EntityNavigationMixin {

    @Shadow
    @Final
    protected Level level;

    @Shadow
    protected Path path;

    @Shadow
    @Final
    protected Mob mob;

    @Inject(
            method = "recomputePath()V",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/world/entity/ai/navigation/PathNavigation;createPath(Lnet/minecraft/core/BlockPos;I)Lnet/minecraft/world/level/pathfinder/Path;",
                    shift = At.Shift.AFTER
            )
    )
    private void updateListeningState(CallbackInfo ci) {
        if (((NavigatingEntity) this.mob).lithium$isRegisteredToWorld()) {
            if (this.path == null) {
                ((ServerWorldExtended) this.level).lithium$setNavigationInactive(this.mob);
            } else {
                ((ServerWorldExtended) this.level).lithium$setNavigationActive(this.mob);
            }
        }
    }

    @Inject(method = "moveTo(Lnet/minecraft/world/level/pathfinder/Path;D)Z", at = @At(value = "RETURN"))
    private void updateListeningState2(Path path, double speed, CallbackInfoReturnable<Boolean> cir) {
        if (((NavigatingEntity) this.mob).lithium$isRegisteredToWorld()) {
            if (this.path == null) {
                ((ServerWorldExtended) this.level).lithium$setNavigationInactive(this.mob);
            } else {
                ((ServerWorldExtended) this.level).lithium$setNavigationActive(this.mob);
            }
        }
    }

    @Inject(method = "stop()V", at = @At(value = "RETURN"))
    private void stopListening(CallbackInfo ci) {
        if (((NavigatingEntity) this.mob).lithium$isRegisteredToWorld()) {
            ((ServerWorldExtended) this.level).lithium$setNavigationInactive(this.mob);
        }
    }
}
