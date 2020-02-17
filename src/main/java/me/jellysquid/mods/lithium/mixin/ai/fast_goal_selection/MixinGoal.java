package me.jellysquid.mods.lithium.mixin.ai.fast_goal_selection;

import me.jellysquid.mods.lithium.common.ai.ExtendedGoal;
import net.minecraft.entity.ai.goal.Goal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumSet;

@Mixin(Goal.class)
public class MixinGoal implements ExtendedGoal {
    private static final Goal.Control[] NO_CONTROLS = new Goal.Control[0];

    private Goal.Control[] controlsArray = NO_CONTROLS;

    /**
     * Initialize our flat controls array to mirror the vanilla EnumSet.
     */
    @Inject(method = "setControls", at = @At("RETURN"))
    private void setControls(EnumSet<Goal.Control> set, CallbackInfo ci) {
        this.controlsArray = set.toArray(NO_CONTROLS); // NO_CONTROLS is only used to get around type erasure
    }

    @Override
    public Goal.Control[] getRequiredControls() {
        return controlsArray;
    }
}
