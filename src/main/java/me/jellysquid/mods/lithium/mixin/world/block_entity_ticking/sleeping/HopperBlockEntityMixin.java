package me.jellysquid.mods.lithium.mixin.world.block_entity_ticking.sleeping;

import me.jellysquid.mods.lithium.common.block.entity.SleepUntilTimeBlockEntityTickInvoker;
import me.jellysquid.mods.lithium.common.block.entity.SleepingBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.BlockEntityTickInvoker;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BooleanSupplier;

@Mixin(HopperBlockEntity.class)
public class HopperBlockEntityMixin extends BlockEntity implements SleepingBlockEntity {

    @Shadow
    private long lastTickTime;
    private WrappedBlockEntityTickInvokerAccessor tickWrapper = null;
    private BlockEntityTickInvoker sleepingTicker = null;

    public HopperBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @SuppressWarnings("InvalidInjectorMethodSignature" )
    @ModifyVariable(
            method = "insertAndExtract",
            at = @At(value = "JUMP", opcode = Opcodes.IFEQ, ordinal = 2),
            argsOnly = true
    )
    private static BooleanSupplier rememberBranch(BooleanSupplier booleanSupplier) {
        return null;
    }

    @Inject(
            method = "insertAndExtract",
            at = @At(value = "RETURN", ordinal = 2)
    )
    private static void sleepIfBranchNotRemembered(World world, BlockPos pos, BlockState state, HopperBlockEntity blockEntity, BooleanSupplier booleanSupplier, CallbackInfoReturnable<Boolean> cir) {
        if (booleanSupplier != null) {
            //When this code is reached, rememberBranch(BooleanSupplier) wasn't reached. Therefore the hopper is locked and not on cooldown.
            ((HopperBlockEntityMixin) (Object) blockEntity).startSleeping();
        }
    }

    private void startSleeping() {
        if (this.tickWrapper == null) {
            return;
        }
        this.lastTickTime = Long.MAX_VALUE;
        this.sleepingTicker = this.tickWrapper.getWrapped();
        this.tickWrapper.setWrapped(SleepingBlockEntity.SLEEPING_BLOCK_ENTITY_TICKER);
    }

    private void wakeUpInNextTick() {
        if (this.sleepingTicker == null || this.tickWrapper == null || this.world == null) {
            return;
        }
        this.tickWrapper.setWrapped(new SleepUntilTimeBlockEntityTickInvoker(this, this.world.getTime() + 1, this.sleepingTicker));
        this.sleepingTicker = null;
    }

    @Inject(
            method = "setTransferCooldown",
            at = @At("HEAD" )
    )
    private void wakeUpOnCooldownSet(int transferCooldown, CallbackInfo ci) {
        if (this.sleepingTicker != null && transferCooldown > 0) {
            this.wakeUpInNextTick();
        }
    }

    @SuppressWarnings("deprecation" )
    @Override
    public void setCachedState(BlockState state) {
        super.setCachedState(state);
        if (this.sleepingTicker != null && state.get(HopperBlock.ENABLED)) {
            this.wakeUpNow();
        }
    }

    private void wakeUpNow() {
        this.setTicker(this.sleepingTicker);
        this.sleepingTicker = null;
    }

    @Override
    public void setWrappedInvoker(WrappedBlockEntityTickInvokerAccessor wrappedBlockEntityTickInvoker) {
        this.tickWrapper = wrappedBlockEntityTickInvoker;
    }

    @Override
    public void setTicker(BlockEntityTickInvoker ticker) {
        if (this.tickWrapper == null) {
            return;
        }
        this.tickWrapper.setWrapped(ticker);
    }
}
