package me.jellysquid.mods.lithium.mixin.entity.skip_fire_check;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    private int fireTicks;

    @Shadow
    protected abstract int getBurningDuration();

    @Shadow
    public boolean wasOnFire;

    @Shadow
    public boolean inPowderSnow;

    @Shadow
    public abstract boolean isWet();

    @Redirect(
            method = "move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;getStatesInBoxIfLoaded(Lnet/minecraft/util/math/Box;)Ljava/util/stream/Stream;"
            )
    )
    private Stream<BlockState> skipFireTestIfResultDoesNotMatter(World world, Box box) {
        // Skip scanning the blocks around the entity touches by returning an empty stream when the result does not matter
        if ((this.fireTicks > 0 || this.fireTicks == -this.getBurningDuration()) && (!this.wasOnFire || !this.inPowderSnow && !this.isWet())) {
            return null;
        }

        return world.getStatesInBoxIfLoaded(box);
    }

    @Redirect(
            method = "move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/stream/Stream;noneMatch(Ljava/util/function/Predicate;)Z"
            )
    )
    private boolean skipNullStream(Stream<BlockState> stream, Predicate<BlockState> predicate) {
        if (stream == null) {
            return true;
        }
        return stream.noneMatch(predicate);
    }
}
