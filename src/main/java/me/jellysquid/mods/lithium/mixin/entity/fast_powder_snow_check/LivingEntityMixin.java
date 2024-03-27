package me.jellysquid.mods.lithium.mixin.entity.fast_powder_snow_check;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Shadow
    @Nullable
    public abstract EntityAttributeInstance getAttributeInstance(RegistryEntry<EntityAttribute> registryEntry);

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Redirect(
            method = "addPowderSnowSlowIfNeeded()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;getLandingBlockState()Lnet/minecraft/block/BlockState;"
            )
    )
    private BlockState delayGetBlockState(LivingEntity instance) {
        return null;
    }

    @Redirect(
            method = "addPowderSnowSlowIfNeeded()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;isAir()Z"
            )
    )
    private boolean delayAirTest(BlockState instance) {
        return false;
    }

    @Redirect(
            method = "addPowderSnowSlowIfNeeded()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;getAttributeInstance(Lnet/minecraft/registry/entry/RegistryEntry;)Lnet/minecraft/entity/attribute/EntityAttributeInstance;"
            )
    )
    private EntityAttributeInstance doDelayedBlockStateAirTest(LivingEntity instance, RegistryEntry<EntityAttribute> registryEntry) {
        //noinspection deprecation
        return this.getLandingBlockState().isAir() ? null : this.getAttributeInstance(registryEntry);
    }
}
