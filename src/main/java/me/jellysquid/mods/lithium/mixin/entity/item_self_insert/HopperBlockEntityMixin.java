package me.jellysquid.mods.lithium.mixin.entity.item_self_insert;

import me.jellysquid.mods.lithium.common.blockentities.HopperAccess;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin extends BlockEntity implements HopperAccess {
    // this represents the number of ticks until the hopper is ready to accept another item
    private int realCooldown;

    @Shadow
    protected abstract boolean isFull();

    @Shadow
    private int transferCooldown;

    public HopperBlockEntityMixin(BlockEntityType<?> type) {
        super(type);
    }

    @Inject(method = "extract(Lnet/minecraft/block/entity/Hopper;)Z", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/HopperBlockEntity;getInputItemEntities(Lnet/minecraft/block/entity/Hopper;)Ljava/util/List;"))
    private static void extract(Hopper hopper, CallbackInfoReturnable<Boolean> cir) {
        if (hopper instanceof HopperBlockEntity && !((HopperBlockEntityMixin) hopper).full()) {
            cir.setReturnValue(false); // this is just to exit the entity searching loop and return
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getTime()J"))
    public void tick(CallbackInfo ci) {//server side only so might as well check if we are on the server ^
        if (this.realCooldown > 0) { // count down counter
            this.realCooldown--;
        }
    }

    @Inject(method = "insertAndExtract", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/HopperBlockEntity;setCooldown(I)V"))
    private void insertAndExtract(Supplier<Boolean> extractMethod, CallbackInfoReturnable<Boolean> cir) {
        this.realCooldown = 8;
    }

    @Override
    public boolean enabled() {
        return this.getCachedState().get(HopperBlock.ENABLED);
    }

    @Override
    public void setCool(int cooldown) {
        this.transferCooldown = cooldown;
        this.realCooldown = cooldown; // update this when accepting items
    }

    @Override
    public int realCooldown() {
        return this.realCooldown;
    }

    public boolean full() {
        return this.isFull();
    }
}
