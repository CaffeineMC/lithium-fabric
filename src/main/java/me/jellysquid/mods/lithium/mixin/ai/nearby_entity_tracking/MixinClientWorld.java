package me.jellysquid.mods.lithium.mixin.ai.nearby_entity_tracking;

import me.jellysquid.mods.lithium.common.entity.EntityTrackerEngine;
import me.jellysquid.mods.lithium.common.entity.WorldWithEntityTrackerEngine;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public class MixinClientWorld {
    @Inject(method = "addEntityPrivate", at = @At(value = "FIELD", target = "Lnet/minecraft/client/world/ClientWorld;regularEntities:Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;"))
    private void onEntityAdded(int id, Entity entity, CallbackInfo ci) {
        if (!(entity instanceof LivingEntity)) {
            return;
        }

        int chunkX = MathHelper.floor(entity.getX() / 16.0D);
        int chunkY = MathHelper.floor(entity.getY() / 16.0D);
        int chunkZ = MathHelper.floor(entity.getZ() / 16.0D);

        EntityTrackerEngine tracker = WorldWithEntityTrackerEngine.getEntityTracker(this);
        tracker.addEntity(chunkX, chunkY, chunkZ, (LivingEntity) entity);
    }

    @Inject(method = "finishRemovingEntity", at = @At(value = "HEAD"))
    private void onEntityRemoved(Entity entity, CallbackInfo ci) {
        if (!(entity instanceof LivingEntity)) {
            return;
        }

        int chunkX = MathHelper.floor(entity.getX() / 16.0D);
        int chunkY = MathHelper.floor(entity.getY() / 16.0D);
        int chunkZ = MathHelper.floor(entity.getZ() / 16.0D);

        EntityTrackerEngine tracker = WorldWithEntityTrackerEngine.getEntityTracker(this);
        tracker.removeEntity(chunkX, chunkY, chunkZ, (LivingEntity) entity);
    }
}
