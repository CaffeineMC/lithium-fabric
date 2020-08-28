package me.jellysquid.mods.lithium.mixin.entity.fire;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.stream.Stream;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow
    private int fireTicks;

    @Shadow
    protected abstract int getBurningDuration();

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;method_29556(Lnet/minecraft/util/math/Box;)Ljava/util/stream/Stream;"))
    private Stream<BlockState> skipFireTestIfResultDoesNotMatter(World world, Box box) {
        if (this.fireTicks > 0) {
            // the 2nd condition this.fireTicks <= 0 is false, so nothing ever happens.
            return Stream.empty();
        } else if (this.fireTicks == -this.getBurningDuration()) {
            // no matter whether there is fire/lava or not, the value won't be changed, as it is already set to the target value.
            return Stream.empty();
        } else {
            // the result of the calculation migth actually affect this.fireTicks, so do the calculation
            return world.method_29556(box);
        }
    }
}
