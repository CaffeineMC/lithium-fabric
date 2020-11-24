package me.jellysquid.mods.lithium.mixin.entity.inactive_navigations;

import me.jellysquid.mods.lithium.common.entity.EntityNavigationExtended;
import me.jellysquid.mods.lithium.common.world.ServerWorldExtended;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityNavigation.class)
public abstract class EntityNavigationMixin implements EntityNavigationExtended {

    @Shadow
    @Final
    protected World world;

    @Shadow
    protected Path currentPath;

    private boolean canListenForBlocks = false;

    @Shadow
    public abstract Path findPathTo(BlockPos target, int distance);

    @Redirect(method = "recalculatePath",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/ai/pathing/EntityNavigation;findPathTo(Lnet/minecraft/util/math/BlockPos;I)Lnet/minecraft/entity/ai/pathing/Path;")
    )
    private Path updateListeningState(EntityNavigation entityNavigation, BlockPos target, int distance) {
        Path pathTo = this.findPathTo(target, distance);
        if (this.canListenForBlocks && ((pathTo == null) != (this.currentPath == null))) {
            if (pathTo == null) {
                ((ServerWorldExtended) this.world).setNavigationInactive(this);
            } else {
                ((ServerWorldExtended) this.world).setNavigationActive(this);
            }
        }
        return pathTo;
    }

    @Inject(method = "startMovingAlong", at = @At(value = "RETURN"))
    private void updateListeningState2(Path path, double speed, CallbackInfoReturnable<Boolean> cir) {
        if (this.canListenForBlocks) {
            if (this.currentPath == null) {
                ((ServerWorldExtended) this.world).setNavigationInactive(this);
            } else {
                ((ServerWorldExtended) this.world).setNavigationActive(this);
            }
        }
    }

    @Inject(method = "stop", at = @At(value = "RETURN"))
    private void stopListening(CallbackInfo ci) {
        if (this.canListenForBlocks) {
            ((ServerWorldExtended) this.world).setNavigationInactive(this);
        }
    }

    @Override
    public void setRegisteredToWorld(boolean isRegistered) {
        //Drowneds are problematic. Their EntityNavigations do not register properly.
        //We make sure to not register them, when vanilla doesn't register them.
        this.canListenForBlocks = isRegistered;
    }
}
