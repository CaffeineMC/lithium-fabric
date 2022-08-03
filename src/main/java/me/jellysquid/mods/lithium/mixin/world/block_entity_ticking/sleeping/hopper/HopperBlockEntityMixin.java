package me.jellysquid.mods.lithium.mixin.world.block_entity_ticking.sleeping.hopper;

import me.jellysquid.mods.lithium.common.block.entity.SleepingBlockEntity;
import me.jellysquid.mods.lithium.common.block.entity.inventory_change_tracking.InventoryChangeListener;
import me.jellysquid.mods.lithium.common.entity.tracker.nearby.NearbyEntityMovementListener;
import me.jellysquid.mods.lithium.mixin.world.block_entity_ticking.sleeping.WrappedBlockEntityTickInvokerAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
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
public class HopperBlockEntityMixin extends BlockEntity implements SleepingBlockEntity, InventoryChangeListener, NearbyEntityMovementListener {

    @Shadow
    private long lastTickTime;

    private WrappedBlockEntityTickInvokerAccessor tickWrapper = null;
    private BlockEntityTickInvoker sleepingTicker = null;

    @Override
    public WrappedBlockEntityTickInvokerAccessor getTickWrapper() {
        return tickWrapper;
    }

    @Override
    public void setTickWrapper(WrappedBlockEntityTickInvokerAccessor tickWrapper) {
        this.tickWrapper = tickWrapper;
    }

    @Override
    public BlockEntityTickInvoker getSleepingTicker() {
        return sleepingTicker;
    }

    @Override
    public void setSleepingTicker(BlockEntityTickInvoker sleepingTicker) {
        this.sleepingTicker = sleepingTicker;
    }

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

    @Override
    public boolean startSleeping() {
        WrappedBlockEntityTickInvokerAccessor tickWrapper = this.getTickWrapper();
        if (tickWrapper != null) {
            this.setSleepingTicker(tickWrapper.getWrapped());
            tickWrapper.callSetWrapped(SleepingBlockEntity.SLEEPING_BLOCK_ENTITY_TICKER);

            // Set the last tick time to max value, so other hoppers transferring into this hopper will set it to 7gt
            // cooldown. Then when waking up, we make sure to not tick this hopper in the same gametick.
            // This makes the observable hopper cooldown not be different from vanilla.
            this.lastTickTime = Long.MAX_VALUE;
            return true;
        }
        return false;
    }

    @Inject(
            method = "setTransferCooldown",
            at = @At("HEAD" )
    )
    private void wakeUpOnCooldownSet(int transferCooldown, CallbackInfo ci) {
        if (transferCooldown == 7) {
            if (this.lastTickTime == Long.MAX_VALUE) {
                this.sleepOnlyCurrentTick();
            }
        } else if (transferCooldown > 0 && this.sleepingTicker != null) {
            this.wakeUpNow();
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

    @Override
    public void handleStackListReplaced(Inventory inventory) {
        this.wakeUpNow();
    }

    @Override
    public void handleInventoryContentModified(Inventory inventory) {
        this.wakeUpNow();
    }

    @Override
    public void handleInventoryRemoved(Inventory inventory) {
        this.wakeUpNow();
    }

    @Override
    public void handleComparatorAdded(Inventory inventory) {
        this.wakeUpNow();
    }

    @Override
    public void handleEntityMovement(Class<?> category) {
        this.wakeUpNow();
    }
}
