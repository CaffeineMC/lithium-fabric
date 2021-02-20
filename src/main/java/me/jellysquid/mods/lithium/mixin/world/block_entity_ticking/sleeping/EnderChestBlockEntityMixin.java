package me.jellysquid.mods.lithium.mixin.world.block_entity_ticking.sleeping;

import me.jellysquid.mods.lithium.common.world.blockentity.BlockEntitySleepTracker;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.EnderChestBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderChestBlockEntity.class)
public class EnderChestBlockEntityMixin extends BlockEntity {
    @Shadow
    public int viewerCount;
    @Shadow
    public float animationProgress;
    @Shadow
    public float lastAnimationProgress;
    @Shadow
    private int ticks;
    private int lastTime;

    public EnderChestBlockEntityMixin(BlockEntityType<?> type) {
        super(type);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void updateTicksOpen(CallbackInfo ci) {
        //noinspection ConstantConditions
        int time = (int) this.world.getTime();
        //ticksOpen == 0 implies most likely that this is the first tick. We don't want to update the value then.
        //overflow case is handles by not going to sleep when this.ticksOpen == 0
        if (this.ticks != 0) {
            this.ticks += time - this.lastTime - 1;
        }
        this.lastTime = time;
    }

    @Inject(method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;addSyncedBlockEvent(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;II)V",
                    shift = At.Shift.BEFORE
            )
    )
    private void checkSleep(CallbackInfo ci) {
        if (this.viewerCount == 0 && this.animationProgress == 0.0F && this.lastAnimationProgress == 0 && this.ticks != 0 && this.world != null) {
            ((BlockEntitySleepTracker)this.world).setAwake(this, false);
        }
    }

    @Inject(method = "onClose", at = @At("RETURN"))
    private void checkWakeUpOnClose(CallbackInfo ci) {
        this.checkWakeUp();
    }
    @Inject(method = "onOpen", at = @At("RETURN"))
    private void checkWakeUpOnOpen(CallbackInfo ci) {
        this.checkWakeUp();
    }
    @Inject(method = "onSyncedBlockEvent", at = @At("RETURN"))
    private void checkWakeUpOnSyncedBlockEvent(int type, int data, CallbackInfoReturnable<Boolean> cir) {
        this.checkWakeUp();
    }

    private void checkWakeUp() {
        if ((this.viewerCount != 0 || this.animationProgress != 0.0F || this.lastAnimationProgress != 0) && this.world != null) {
            ((BlockEntitySleepTracker)this.world).setAwake(this, true);
        }
    }
}
