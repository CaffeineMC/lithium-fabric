package me.jellysquid.mods.lithium.mixin.world.block_entity_ticking.sleeping;

import me.jellysquid.mods.lithium.common.world.blockentity.BlockEntitySleepTracker;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChestBlockEntity.class)
public class ChestBlockEntityMixin extends BlockEntity {
    @Shadow
    protected int viewerCount;

    @Shadow
    protected float animationAngle;

    @Shadow
    protected float lastAnimationAngle;

    @Shadow
    private int ticksOpen;

    private int lastTime;

    public ChestBlockEntityMixin(BlockEntityType<?> type) {
        super(type);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void updateTicksOpen(CallbackInfo ci) {
        //noinspection ConstantConditions
        int time = (int) this.world.getTime();
        //ticksOpen == 0 implies most likely that this is the first tick. We don't want to update the value then.
        //overflow case is handled by not going to sleep when this.ticksOpen == 0
        if (this.ticksOpen != 0) {
            this.ticksOpen += time - this.lastTime - 1;
        }
        this.lastTime = time;
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void checkSleep(CallbackInfo ci) {
        if (this.viewerCount == 0 && this.animationAngle == 0.0F && this.lastAnimationAngle == 0 && this.ticksOpen != 0 && this.world != null) {
            ((BlockEntitySleepTracker)this.world).setAwake(this, false);
        }
    }

    @Inject(method = "onClose", at = @At("RETURN"))
    private void checkWakeUpOnClose(PlayerEntity player, CallbackInfo ci) {
        this.checkWakeUp();
    }
    @Inject(method = "onOpen", at = @At("RETURN"))
    private void checkWakeUpOnOpen(PlayerEntity player, CallbackInfo ci) {
        this.checkWakeUp();
    }
    @Inject(method = "onSyncedBlockEvent", at = @At("RETURN"))
    private void checkWakeUpOnSyncedBlockEvent(int type, int data, CallbackInfoReturnable<Boolean> cir) {
        this.checkWakeUp();
    }

    private void checkWakeUp() {
        if ((this.viewerCount != 0 || this.animationAngle != 0.0F || this.lastAnimationAngle != 0) && this.world != null) {
            ((BlockEntitySleepTracker)this.world).setAwake(this, true);
        }
    }
}
