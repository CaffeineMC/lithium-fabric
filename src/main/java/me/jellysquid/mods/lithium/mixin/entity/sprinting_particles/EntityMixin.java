package me.jellysquid.mods.lithium.mixin.entity.sprinting_particles;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow
    public abstract World getWorld();

    @Redirect(
            method = "baseTick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;shouldSpawnSprintingParticles()Z")
    )
    private boolean skipParticlesOnServerSide(Entity instance) {
        if (instance.getWorld().isClient()) {
            return instance.shouldSpawnSprintingParticles();
        }
        return false;
    }
}
