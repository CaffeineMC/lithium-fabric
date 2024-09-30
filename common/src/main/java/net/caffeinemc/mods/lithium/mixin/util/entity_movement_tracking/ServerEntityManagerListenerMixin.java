package net.caffeinemc.mods.lithium.mixin.util.entity_movement_tracking;

import net.caffeinemc.mods.lithium.common.entity.movement_tracker.EntityMovementTrackerSection;
import net.caffeinemc.mods.lithium.common.entity.movement_tracker.MovementTrackerHelper;
import net.caffeinemc.mods.lithium.common.entity.movement_tracker.ToggleableMovementTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.level.entity.Visibility;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(targets = "net/minecraft/world/level/entity/PersistentEntitySectionManager$Callback")
public class ServerEntityManagerListenerMixin<T extends EntityAccess> implements ToggleableMovementTracker {
    @Shadow
    private EntitySection<T> currentSection;
    @Shadow
    @Final
    private T entity;

    private int notificationMask;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(PersistentEntitySectionManager<?> outer, T entityLike, long l, EntitySection<T> entityTrackingSection, CallbackInfo ci) {
        this.notificationMask = MovementTrackerHelper.getNotificationMask(this.entity.getClass());

        //Fix #284 Summoned inventory minecarts do not immediately notify hoppers of their presence when created using summon command
        this.notifyMovementListeners();
    }

    @Inject(method = "onMove()V", at = @At("RETURN"))
    private void updateEntityTrackerEngine(CallbackInfo ci) {
        this.notifyMovementListeners();
    }

    @Inject(
            method = "onMove()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/entity/EntitySection;add(Lnet/minecraft/world/level/entity/EntityAccess;)V",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void onAddEntity(CallbackInfo ci, BlockPos blockPos, long newPos, Visibility entityTrackingStatus, EntitySection<T> entityTrackingSection) {
        this.notifyMovementListeners();
    }

    @Inject(
            method = "onRemove(Lnet/minecraft/world/entity/Entity$RemovalReason;)V",
            at = @At(
                    value = "HEAD"
            )
    )
    private void onRemoveEntity(Entity.RemovalReason reason, CallbackInfo ci) {
        this.notifyMovementListeners();
    }

    private void notifyMovementListeners() {
        if (this.notificationMask != 0) {
            ((EntityMovementTrackerSection) this.currentSection).lithium$trackEntityMovement(this.notificationMask, ((Entity) this.entity).getCommandSenderWorld().getGameTime());
        }
    }

    @Override
    public int lithium$setNotificationMask(int notificationMask) {
        int oldNotificationMask = this.notificationMask;
        this.notificationMask = notificationMask;
        return oldNotificationMask;
    }
}
