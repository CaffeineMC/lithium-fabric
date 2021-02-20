package me.jellysquid.mods.lithium.mixin.world.block_entity_ticking.sleeping;

import me.jellysquid.mods.lithium.common.world.blockentity.BlockEntitySleepTracker;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShulkerBoxBlockEntity.class)
public abstract class ShulkerBoxBlockEntityMixin extends BlockEntity {

    public ShulkerBoxBlockEntityMixin(BlockEntityType<?> type) {
        super(type);
    }

    @Shadow
    public abstract ShulkerBoxBlockEntity.AnimationStage getAnimationStage();

    @Shadow
    private float prevAnimationProgress;

    @Shadow
    private float animationProgress;

    @Inject(method = "tick", at = @At("RETURN"))
    private void checkSleep(CallbackInfo ci) {
        if (this.getAnimationStage() == ShulkerBoxBlockEntity.AnimationStage.CLOSED && this.prevAnimationProgress == 0f &&
                this.animationProgress == 0f && this.world != null) {
            ((BlockEntitySleepTracker)this.world).setAwake(this, false);
        }
    }

    @Inject(method = "onSyncedBlockEvent", at = @At("HEAD"))
    public void checkWakeUp(int type, int data, CallbackInfoReturnable<Boolean> cir) {
        if (this.world != null && type == 1) {
            ((BlockEntitySleepTracker)this.world).setAwake(this, true);
        }
    }
}
