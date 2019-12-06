package me.jellysquid.mods.lithium.mixin.entity.item_self_insert;

import me.jellysquid.mods.lithium.common.blockentities.HopperAccess;
import net.minecraft.block.BlockState;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin extends BlockEntity implements HopperAccess {

    @Shadow
    protected abstract boolean isFull();

    @Shadow
    private int transferCooldown;

    public boolean full() {
        return this.isFull();
    }

    public HopperBlockEntityMixin(BlockEntityType<?> type) {
        super(type);
    }

    @Inject(method = "extract(Lnet/minecraft/block/entity/Hopper;)Z", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/HopperBlockEntity;getInputItemEntities(Lnet/minecraft/block/entity/Hopper;)Ljava/util/List;"))
    private static void extract(Hopper hopper, CallbackInfoReturnable<Boolean> cir) {
        if (hopper instanceof HopperBlockEntity && !((HopperBlockEntityMixin) hopper).full()) {
            cir.setReturnValue(true); // this is just to exit the entity searching loop and return
        }
    }

    @Override
    public boolean enabled() {
        return this.getCachedState().get(HopperBlock.ENABLED);
    }

    @Override
    public void setCooldown(int cooldown) {
        this.transferCooldown = cooldown;
    }

    @Override
    public int getCooldown() {
        return this.transferCooldown;
    }
}
