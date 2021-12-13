package me.jellysquid.mods.lithium.mixin.alloc.vectors;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityTrackerEntry.class)
public class EntityTrackerEntryMixin {

    @Unique
    private double lithium$x;
    @Unique
    private double lithium$y;
    @Unique
    private double lithium$z;

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/math/Vec3d;subtract(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;"
            )
    )
    private Vec3d removeAllocation(Vec3d playerPos, Vec3d playerLastPos) {
        // Magic numbers from EntityS2CPacket.decodePacketCoordinates(ddd)
        this.lithium$x = this.entity.getPos().x - (this.lastX * 2.44140625E-4D);
        this.lithium$y = this.entity.getPos().y - (this.lastY * 2.44140625E-4D);
        this.lithium$z = this.entity.getPos().z - (this.lastZ * 2.44140625E-4D);

        return null;
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;lengthSquared()D"))
    private double calcVecSquaredLength(Vec3d vec) {
        return this.lithium$x * this.lithium$x + this.lithium$y * this.lithium$y + this.lithium$z * this.lithium$z;
    }

    @Redirect(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/util/math/Vec3d;x:D"))
    private double useX(Vec3d vec) {
        return this.lithium$x;
    }

    @Redirect(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/util/math/Vec3d;y:D"))
    private double useY(Vec3d vec) {
        return this.lithium$y;
    }

    @Redirect(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/util/math/Vec3d;z:D"))
    private double useZ(Vec3d vec) {
        return this.lithium$z;
    }

    @Shadow
    @Final
    private Entity entity;
    @Shadow
    private long lastX;
    @Shadow
    private long lastY;
    @Shadow
    private long lastZ;
}
