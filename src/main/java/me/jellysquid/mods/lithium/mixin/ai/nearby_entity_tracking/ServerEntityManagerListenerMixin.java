package me.jellysquid.mods.lithium.mixin.ai.nearby_entity_tracking;

import me.jellysquid.mods.lithium.common.entity.tracker.EntityTrackerEngine;
import me.jellysquid.mods.lithium.common.entity.tracker.nearby.NearbyEntityListenerMulti;
import me.jellysquid.mods.lithium.common.entity.tracker.nearby.NearbyEntityListenerProvider;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.entity.EntityTrackingSection;
import net.minecraft.world.entity.EntityTrackingStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(targets = "net/minecraft/server/world/ServerEntityManager$Listener")
public class ServerEntityManagerListenerMixin<T extends EntityLike> {
    @Shadow
    private EntityTrackingSection<T> section;
    @Shadow
    @Final
    private T entity;

    @SuppressWarnings("ShadowTarget")
    @Shadow
    ServerEntityManager<T> manager;

    @Shadow
    private long sectionPos;

    private int notificationMask;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(ServerEntityManager<?> outer, T entityLike, long l, EntityTrackingSection<T> entityTrackingSection, CallbackInfo ci) {
        this.notificationMask = EntityTrackerEngine.getNotificationMask(this.entity.getClass());
    }

    @Inject(method = "updateEntityPosition()V", at = @At("RETURN"))
    private void updateEntityTrackerEngine(CallbackInfo ci) {
        if (this.notificationMask != 0) {
            ((EntityTrackerEngine.EntityTrackingSectionAccessor) this.section).updateMovementTimestamps(this.notificationMask, ((Entity) this.entity).getEntityWorld().getTime());
        }
    }

    @Inject(
            method = "updateEntityPosition()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/EntityTrackingSection;add(Ljava/lang/Object;)V",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void onAddEntity(CallbackInfo ci, BlockPos blockPos, long newPos, EntityTrackingStatus entityTrackingStatus, EntityTrackingSection<T> entityTrackingSection) {
        NearbyEntityListenerMulti listener = ((NearbyEntityListenerProvider) entity).getListener();
        if (listener != null)
        {
            listener.forEachChunkInRangeChange(
                    ((ServerEntityManagerAccessor) this.manager).getCache(),
                    ChunkSectionPos.from(this.sectionPos),
                    ChunkSectionPos.from(newPos),
                    EntityTrackerEngine.enteredRangeConsumer,
                    EntityTrackerEngine.leftRangeConsumer
            );
        }
        if (this.notificationMask != 0) {
            ((EntityTrackerEngine.EntityTrackingSectionAccessor) this.section).updateMovementTimestamps(this.notificationMask, ((Entity) this.entity).getEntityWorld().getTime());
        }
    }

    @Inject(
            method = "remove(Lnet/minecraft/entity/Entity$RemovalReason;)V",
            at = @At(
                    value = "HEAD"
            )
    )
    private void onRemoveEntity(Entity.RemovalReason reason, CallbackInfo ci) {
        NearbyEntityListenerMulti listener = ((NearbyEntityListenerProvider) entity).getListener();
        if (listener != null)
        {
            listener.forEachChunkInRangeChange(
                    ((ServerEntityManagerAccessor) this.manager).getCache(),
                    ChunkSectionPos.from(this.sectionPos),
                    null,
                    null,
                    EntityTrackerEngine.leftRangeConsumer
            );
        }
        if (this.notificationMask != 0) {
            ((EntityTrackerEngine.EntityTrackingSectionAccessor) this.section).updateMovementTimestamps(this.notificationMask, ((Entity) this.entity).getEntityWorld().getTime());
        }
    }
}
