package me.jellysquid.mods.lithium.mixin.ai.task.run.long_jump_weighted_choice;

import com.llamalad7.mixinextras.sugar.Local;
import me.jellysquid.mods.lithium.common.util.collections.LongJumpChoiceList;
import net.minecraft.entity.ai.brain.task.LongJumpTask;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.Weighting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Mixin(LongJumpTask.class)
public class LongJumpTaskMixin<E extends MobEntity> {

    @Shadow
    protected List<LongJumpTask.Target> targets;

    @Shadow
    @Final
    protected int horizontalRange;

    @Shadow
    @Final
    protected int verticalRange;

    @Inject(
            method = "run(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/mob/MobEntity;J)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/math/BlockPos;stream(IIIIII)Ljava/util/stream/Stream;"
            ),
            cancellable = true
    )
    private void setTargets(ServerWorld serverWorld, E mobEntity, long l, CallbackInfo ci, @Local BlockPos centerPos) {
        if (this.horizontalRange < 128 && this.verticalRange < 128) {
            this.targets = LongJumpChoiceList.forCenter(centerPos, (byte) this.horizontalRange, (byte) this.verticalRange);
            ci.cancel();
        }
    }

    @Redirect(
            method = "getTarget(Lnet/minecraft/server/world/ServerWorld;)Ljava/util/Optional;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/Weighting;getRandom(Lnet/minecraft/util/math/random/Random;Ljava/util/List;)Ljava/util/Optional;")
    )
    private Optional<LongJumpTask.Target> getRandomFast(Random random, List<LongJumpTask.Target> pool) {
        if (pool instanceof LongJumpChoiceList longJumpChoiceList) {
            return Optional.ofNullable(longJumpChoiceList.removeRandomWeightedByDistanceSq(random));
        } else {
            return Weighting.getRandom(random, pool);
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Redirect(
            method = "getTarget(Lnet/minecraft/server/world/ServerWorld;)Ljava/util/Optional;",
            at = @At(value = "INVOKE", target = "Ljava/util/Optional;ifPresent(Ljava/util/function/Consumer;)V")
    )
    private void skipRemoveIfAlreadyRemoved(Optional<LongJumpTask.Target> result, Consumer<? super LongJumpTask.Target> removeAction) {
        if (!(this.targets instanceof LongJumpChoiceList)) {
            result.ifPresent(removeAction);
        }
    }
}
