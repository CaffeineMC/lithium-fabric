package me.jellysquid.mods.lithium.mixin.ai.nearby_entity_tracking;

import me.jellysquid.mods.lithium.common.entity.tracker.EntityTrackerEngineProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldChunk.class)
public class WorldChunkMixin {

    @Shadow
    @Final
    private World world;

    @Shadow
    @Final
    private ChunkPos pos;

    @Inject(method = "addEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/TypeFilterableList;add(Ljava/lang/Object;)Z"))
    private void onEntityAdded(Entity entity, CallbackInfo ci) {
        if (entity instanceof LivingEntity) {
            EntityTrackerEngineProvider.getEntityTracker(this.world).onEntityAdded(entity.chunkX, entity.chunkY, entity.chunkZ, (LivingEntity) entity);
        }
    }

    @Inject(method = "remove(Lnet/minecraft/entity/Entity;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/TypeFilterableList;remove(Ljava/lang/Object;)Z"))
    private void onEntityRemoved(Entity entity, int section, CallbackInfo ci) {
        if (entity instanceof LivingEntity) {
            EntityTrackerEngineProvider.getEntityTracker(this.world).onEntityRemoved(this.pos.x, section, this.pos.z, (LivingEntity) entity);
        }
    }
}
