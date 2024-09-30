package me.jellysquid.mods.lithium.mixin.collections.goals;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;

@Mixin(GoalSelector.class)
public abstract class GoalSelectorMixin {

    @Mutable
    @Shadow
    @Final
    private Set<WrappedGoal> availableGoals;

    /**
     * Replace the goal set with an optimized collection type which performs better for iteration.
     */
    @Inject(method = "<init>(Ljava/util/function/Supplier;)V", at = @At("RETURN"))
    private void reinit(Supplier<ProfilerFiller> supplier, CallbackInfo ci) {
        this.availableGoals = new ObjectLinkedOpenHashSet<>(this.availableGoals);
    }
}
