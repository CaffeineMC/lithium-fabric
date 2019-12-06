package me.jellysquid.mods.lithium.mixin.entity.item_self_insert.standard;

import me.jellysquid.mods.lithium.common.blockentities.HopperCooldown;
import net.minecraft.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import java.util.function.Supplier;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin implements HopperCooldown {
	@Shadow private int transferCooldown;

	@Redirect (method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/HopperBlockEntity;insertAndExtract(Ljava/util/function/Supplier;)Z"))
	public boolean tick(HopperBlockEntity entity, Supplier<Boolean> extractMethod) {
		return false;
	}

	@Override
	public int cooldown() {
		return transferCooldown;
	}
}
