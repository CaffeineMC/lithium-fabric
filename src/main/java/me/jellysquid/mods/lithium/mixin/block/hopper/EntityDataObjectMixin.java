package me.jellysquid.mods.lithium.mixin.block.hopper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.commands.data.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fixes hoppers not noticing that the item type of item entities changed after running the data command.
 */
@Mixin(EntityDataAccessor.class)
public class EntityDataObjectMixin {
    @Shadow
    @Final
    private Entity entity;

    @Inject(
            method = "setData",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;setUUID(Ljava/util/UUID;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void updateEntityTrackerEngine(CompoundTag nbt, CallbackInfo ci) {
        Entity entity = this.entity;
        if (entity instanceof ItemEntity) {
            ((EntityAccessor) entity).getChangeListener().onMove();
        }
    }
}
