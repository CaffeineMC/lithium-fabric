package me.jellysquid.mods.lithium.mixin.world.block_entity_ticking.sleeping;

import me.jellysquid.mods.lithium.common.world.blockentity.BlockEntitySleepTracker;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CampfireBlockEntity.class)
public class CampfireBlockEntityMixin extends BlockEntity {
    @Shadow
    @Final
    private DefaultedList<ItemStack> itemsBeingCooked;

    @Shadow
    @Final
    private int[] cookingTimes;
    @Unique
    private boolean isTicking = true;
    @Unique
    private boolean doInit = true;

    public CampfireBlockEntityMixin(BlockEntityType<?> type) {
        super(type);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void firstTick(CallbackInfo ci) {
        if (this.doInit) {
            this.doInit = false;
            this.checkSleepState();
        }
    }
    @Inject(method = "fromTag", at = @At("RETURN"))
    private void wakeUpAfterFromTag(CallbackInfo ci) {
        this.checkSleepState();
    }

    private void checkSleepState() {
        if (this.world == null || this.world.isClient()) {
            return;
        }
        boolean shouldTick = false;
        DefaultedList<ItemStack> beingCooked = this.itemsBeingCooked;
        for (int i = 0; i < beingCooked.size(); i++) {
            ItemStack stack = beingCooked.get(i);
            if (!stack.isEmpty()) {
                if (this.cookingTimes[i] > 0 || this.getCachedState().get(CampfireBlock.LIT)) {
                    shouldTick = true;
                    break;
                }
            }
        }

        if (shouldTick != this.isTicking) {
            this.isTicking = shouldTick;
            ((BlockEntitySleepTracker)this.world).setAwake(this, shouldTick);
        }
    }

    @Override
    public void markDirty() {
        super.markDirty();
        this.checkSleepState();
    }

    @Override
    public void resetBlock() {
        super.resetBlock();
        this.checkSleepState();
    }
}
