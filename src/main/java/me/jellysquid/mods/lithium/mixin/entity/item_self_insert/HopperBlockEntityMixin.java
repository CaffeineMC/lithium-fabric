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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin extends BlockEntity implements HopperAccess {
    public HopperBlockEntityMixin(BlockEntityType<?> type) { super(type); }

    @Inject(method = "extract(Lnet/minecraft/block/entity/Hopper;)Z",cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/HopperBlockEntity;getInputItemEntities(Lnet/minecraft/block/entity/Hopper;)Ljava/util/List;"))
    private static void extract(Hopper hopper, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(false);
	}

    @Override
    public boolean enabled() {
        return this.getCachedState().get(HopperBlock.ENABLED);
    }
}
