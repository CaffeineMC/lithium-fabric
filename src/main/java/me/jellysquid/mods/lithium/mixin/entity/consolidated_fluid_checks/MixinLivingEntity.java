package me.jellysquid.mods.lithium.mixin.entity.consolidated_fluid_checks;

import me.jellysquid.mods.lithium.common.entity.fluids.ResettableFluidCache;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {
    public MixinLivingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Redirect(method = "fall", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;checkWaterState()V", ordinal = 0))
    private void clearCacheAndCheckWaterState(LivingEntity livingEntity) {
        ((ResettableFluidCache) this).resetFluidCache();
        this.checkWaterState();
    }
}
