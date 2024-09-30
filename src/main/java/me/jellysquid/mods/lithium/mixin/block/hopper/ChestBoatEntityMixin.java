package me.jellysquid.mods.lithium.mixin.block.hopper;

import me.jellysquid.mods.lithium.common.entity.movement_tracker.ToggleableMovementTracker;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChestBoat.class)
public abstract class ChestBoatEntityMixin extends Entity {
    public ChestBoatEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Intrinsic
    @Override
    public void rideTick() {
        super.rideTick();
    }

    @SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference"})
    @Redirect(
            method = "rideTick()V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;rideTick()V")
    )
    private void tickRidingSummarizeMovementNotifications(Entity entity) {
        EntityInLevelCallback changeListener = ((EntityAccessor) this).getChangeListener();
        if (changeListener instanceof ToggleableMovementTracker toggleableMovementTracker) {
            Vec3 beforeTickPos = this.position();
            int beforeMovementNotificationMask = toggleableMovementTracker.lithium$setNotificationMask(0);

            super.rideTick();

            toggleableMovementTracker.lithium$setNotificationMask(beforeMovementNotificationMask);

            if (!beforeTickPos.equals(this.position())) {
                changeListener.onMove();
            }
        } else {
            super.rideTick();
        }
    }
}
