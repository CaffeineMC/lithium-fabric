package me.jellysquid.mods.lithium.mixin.entity.item_self_insert;

import me.jellysquid.mods.lithium.common.blockentities.HopperAccess;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Supplier;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin extends BlockEntity implements HopperAccess {
	@Shadow private int transferCooldown;

    public HopperBlockEntityMixin(BlockEntityType<?> type) { super(type); }

    @Redirect (method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/HopperBlockEntity;insertAndExtract(Ljava/util/function/Supplier;)Z"))
	public boolean tick(HopperBlockEntity entity, Supplier<Boolean> extractMethod) {
		return false;
	}

	@Override
	public int cooldown() {
		return this.transferCooldown;
	}

    @Override
    public boolean enabled() {
        return this.getCachedState().get(HopperBlock.ENABLED);
    }
}
