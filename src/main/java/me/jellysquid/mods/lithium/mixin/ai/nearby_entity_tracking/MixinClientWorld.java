package me.jellysquid.mods.lithium.mixin.ai.nearby_entity_tracking;

import me.jellysquid.mods.lithium.common.entity.tracker.EntityTrackerEngine;
import me.jellysquid.mods.lithium.common.entity.tracker.WorldWithEntityTrackerEngine;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Installs event listeners to the world class which will be used to notify the {@link EntityTrackerEngine} of changes.
 */
@Mixin(ClientWorld.class)
public class MixinClientWorld {
    /**
     * Notify the entity tracker when an entity moves and enters a new chunk.
     */
    @Inject(method = "checkChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/WorldChunk;addEntity(Lnet/minecraft/entity/Entity;)V", shift = At.Shift.BEFORE))
    private void onEntityMoveAdd(Entity entity, CallbackInfo ci) {
        if (!(entity instanceof LivingEntity)) {
            return;
        }

        int x = MathHelper.floor(entity.getX()) >> 4;
        int y = MathHelper.floor(entity.getY()) >> 4;
        int z = MathHelper.floor(entity.getZ()) >> 4;

        EntityTrackerEngine tracker = WorldWithEntityTrackerEngine.getEntityTracker(this);
        tracker.onEntityAdded(x, y, z, (LivingEntity) entity);

    }

    /**
     * Notify the entity tracker when an entity moves and is removed from the previous chunk.
     */
    @Inject(method = "checkChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/WorldChunk;remove(Lnet/minecraft/entity/Entity;I)V", shift = At.Shift.BEFORE))
    private void onEntityMoveRemove(Entity entity, CallbackInfo ci) {
        if (!(entity instanceof LivingEntity)) {
            return;
        }

        // The chunkX/Y/Z fields on the entity represent the entity's chunk coordinates in the *previous* tick
        EntityTrackerEngine tracker = WorldWithEntityTrackerEngine.getEntityTracker(this);
        tracker.onEntityRemoved(entity.chunkX, entity.chunkY, entity.chunkZ, (LivingEntity) entity);
    }

    /**
     * Notify the entity tracker when an entity is added to the world.
     */
    @Inject(method = "addEntityPrivate", at = @At(value = "FIELD", target = "Lnet/minecraft/client/world/ClientWorld;regularEntities:Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;"))
    private void onEntityAdded(int id, Entity entity, CallbackInfo ci) {
        if (!(entity instanceof LivingEntity)) {
            return;
        }

        int chunkX = MathHelper.floor(entity.getX()) >> 4;
        int chunkY = MathHelper.floor(entity.getY()) >> 4;
        int chunkZ = MathHelper.floor(entity.getZ()) >> 4;

        EntityTrackerEngine tracker = WorldWithEntityTrackerEngine.getEntityTracker(this);
        tracker.onEntityAdded(chunkX, chunkY, chunkZ, (LivingEntity) entity);
    }

    /**
     * Notify the entity tracker when an entity is removed from the world.
     */
    @Inject(method = "finishRemovingEntity", at = @At(value = "HEAD"))
    private void onEntityRemoved(Entity entity, CallbackInfo ci) {
        if (!(entity instanceof LivingEntity)) {
            return;
        }

        int chunkX = MathHelper.floor(entity.getX()) >> 4;
        int chunkY = MathHelper.floor(entity.getY()) >> 4;
        int chunkZ = MathHelper.floor(entity.getZ()) >> 4;

        EntityTrackerEngine tracker = WorldWithEntityTrackerEngine.getEntityTracker(this);
        tracker.onEntityRemoved(chunkX, chunkY, chunkZ, (LivingEntity) entity);
    }
}
