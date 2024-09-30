package me.jellysquid.mods.lithium.mixin.world.block_entity_ticking.sleeping.hopper;

import me.jellysquid.mods.lithium.common.block.entity.SleepingBlockEntity;
import me.jellysquid.mods.lithium.mixin.world.block_entity_ticking.sleeping.WrappedBlockEntityTickInvokerAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BooleanSupplier;

@Mixin(HopperBlockEntity.class)
public class HopperBlockEntityMixin extends BlockEntity implements SleepingBlockEntity {

    @Shadow
    private long tickedGameTime;

    @Shadow
    private native boolean isOnCooldown();

    private WrappedBlockEntityTickInvokerAccessor tickWrapper = null;
    private TickingBlockEntity sleepingTicker = null;

    @Inject(
            method = "tryMoveItems(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/HopperBlockEntity;Ljava/util/function/BooleanSupplier;)Z",
            at = @At(value = "RETURN", ordinal = 2)
    )
    private static void sleepIfNoCooldownAndLocked(Level world, BlockPos pos, BlockState state, HopperBlockEntity blockEntity, BooleanSupplier booleanSupplier, CallbackInfoReturnable<Boolean> cir) {
        if (!((HopperBlockEntityMixin) (Object) blockEntity).isOnCooldown() &&
                !((HopperBlockEntityMixin) (Object) blockEntity).isSleeping() &&
                !state.getValue(HopperBlock.ENABLED)) {
            ((HopperBlockEntityMixin) (Object) blockEntity).lithium$startSleeping();
        }
    }

    @Override
    public WrappedBlockEntityTickInvokerAccessor lithium$getTickWrapper() {
        return tickWrapper;
    }

    @Override
    public void lithium$setTickWrapper(WrappedBlockEntityTickInvokerAccessor tickWrapper) {
        this.tickWrapper = tickWrapper;
        this.lithium$setSleepingTicker(null);
    }

    @Override
    public TickingBlockEntity lithium$getSleepingTicker() {
        return sleepingTicker;
    }

    public HopperBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void lithium$setSleepingTicker(TickingBlockEntity sleepingTicker) {
        this.sleepingTicker = sleepingTicker;
    }

    @Override
    public boolean lithium$startSleeping() {
        if (this.isSleeping()) {
            return false;
        }

        WrappedBlockEntityTickInvokerAccessor tickWrapper = this.lithium$getTickWrapper();
        if (tickWrapper != null) {
            this.lithium$setSleepingTicker(tickWrapper.getWrapped());
            tickWrapper.callSetWrapped(SleepingBlockEntity.SLEEPING_BLOCK_ENTITY_TICKER);

            // Set the last tick time to max value, so other hoppers transferring into this hopper will set it to 7gt
            // cooldown. Then when waking up, we make sure to not tick this hopper in the same gametick.
            // This makes the observable hopper cooldown not be different from vanilla.
            this.tickedGameTime = Long.MAX_VALUE;
            return true;
        }
        return false;
    }

    @Inject(
            method = "setCooldown(I)V",
            at = @At("HEAD" )
    )
    private void wakeUpOnCooldownSet(int transferCooldown, CallbackInfo ci) {
        if (transferCooldown == 7) {
            if (this.tickedGameTime == Long.MAX_VALUE) {
                this.sleepOnlyCurrentTick();
            } else {
                this.wakeUpNow();
            }
        } else if (transferCooldown > 0 && this.sleepingTicker != null) {
            this.wakeUpNow();
        }
    }
}
