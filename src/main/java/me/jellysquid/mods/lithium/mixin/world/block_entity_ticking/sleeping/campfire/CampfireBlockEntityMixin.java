package me.jellysquid.mods.lithium.mixin.world.block_entity_ticking.sleeping.campfire;

import me.jellysquid.mods.lithium.common.block.entity.SleepingBlockEntity;
import me.jellysquid.mods.lithium.mixin.world.block_entity_ticking.sleeping.WrappedBlockEntityTickInvokerAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.BlockEntityTickInvoker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CampfireBlockEntity.class)
public class CampfireBlockEntityMixin extends BlockEntity implements SleepingBlockEntity {

    @Shadow
    @Final
    private int[] cookingTimes;
    @Shadow
    @Final
    private DefaultedList<ItemStack> itemsBeingCooked;
    private WrappedBlockEntityTickInvokerAccessor tickWrapper = null;
    private BlockEntityTickInvoker sleepingTicker = null;

    public CampfireBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

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


    @Inject(
            method = "addItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/DefaultedList;set(ILjava/lang/Object;)Ljava/lang/Object;" )
    )
    private void wakeUpOnAddItem(Entity user, ItemStack stack, int cookTime, CallbackInfoReturnable<Boolean> cir) {
        this.wakeUpNow();
    }

    @Inject(
            method = "unlitServerTick",
            at = @At("RETURN" ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void trySleepUnlit(World world, BlockPos pos, BlockState state, CampfireBlockEntity campfire, CallbackInfo ci, boolean hadProgress) {
        if (!hadProgress) {
            CampfireBlockEntityMixin self = (CampfireBlockEntityMixin) (Object) campfire;
            self.startSleeping();
        }
    }

    @Inject(
            method = "litServerTick",
            at = @At("RETURN" ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void trySleepLit(World world, BlockPos pos, BlockState state, CampfireBlockEntity campfire, CallbackInfo ci, boolean hadProgress) {
        if (!hadProgress) {
            CampfireBlockEntityMixin self = (CampfireBlockEntityMixin) (Object) campfire;
            self.startSleeping();
        }
    }
}
