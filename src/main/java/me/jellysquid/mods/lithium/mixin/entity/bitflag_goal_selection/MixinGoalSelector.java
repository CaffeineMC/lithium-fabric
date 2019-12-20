package me.jellysquid.mods.lithium.mixin.entity.bitflag_goal_selection;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.WeightedGoal;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(GoalSelector.class)
public abstract class MixinGoalSelector {
    @Shadow
    @Final
    private Profiler profiler;

    @Shadow
    @Final
    private Set<WeightedGoal> goals;

    private boolean[] disabledControlsArray;

    private WeightedGoal[] goalsByControlArray;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(Profiler profiler, CallbackInfo ci) {
        this.disabledControlsArray = new boolean[4];
        this.goalsByControlArray = new WeightedGoal[4];
    }

    /**
     * Makes use of a simple ordinal-indexed array to track the controls of each goal. We also avoid the usage of
     * streams entirely to squeeze out additional performance.
     *
     * @reason Remove lambdas and complex stream logic
     * @author JellySquid
     */
    @Overwrite
    public void tick() {
        this.selectGoals();
        this.tickGoals();
    }

    private void selectGoals() {
        this.profiler.push("goalUpdate");

        // Stop goals which are disabled
        for (WeightedGoal goal : this.goals) {
            if (!goal.isRunning()) {
                continue;
            }

            // If the goal shouldn't continue or any of its controls have been disabled, then stop the goal
            if (!goal.shouldContinue() || this.anyControlsDisabled(goal)) {
                goal.stop();
            }
        }

        // Disassociate controls from stopped goals
        for (int i = 0; i < this.goalsByControlArray.length; i++) {
            WeightedGoal goal = this.goalsByControlArray[i];

            if (goal != null && !goal.isRunning()) {
                this.goalsByControlArray[i] = null;
            }
        }

        // Try to start new goals where possible
        for (WeightedGoal goal : this.goals) {
            // Filter out goals which can be started
            if (goal.isRunning() || !goal.canStart()) {
                continue;
            }

            // Check if the goal's controls are available or can be replaced
            if (!this.areGoalControlsAvailableForReplacement(goal)) {
                continue;
            }

            // Hand over controls to this goal and stop any goals which depended on those controls
            for (Goal.Control control : goal.getControls()) {
                WeightedGoal otherGoal = this.getGoalForControl(control);

                if (otherGoal != null) {
                    otherGoal.stop();
                }

                this.setGoalForControl(control, goal);
            }

            goal.start();
        }

        this.profiler.pop();
    }

    private void tickGoals() {
        this.profiler.push("goalTick");

        for (WeightedGoal goal : this.goals) {
            if (goal.isRunning()) {
                goal.tick();
            }
        }

        this.profiler.pop();
    }

    private boolean anyControlsDisabled(WeightedGoal goal) {
        for (Goal.Control control : goal.getControls()) {
            if (this.isControlDisabled(control)) {
                return true;
            }
        }

        return false;
    }

    private boolean areGoalControlsAvailableForReplacement(WeightedGoal goal) {
        for (Goal.Control control : goal.getControls()) {
            if (this.isControlDisabled(control)) {
                return false;
            }

            WeightedGoal occupied = this.getGoalForControl(control);

            if (occupied != null && !occupied.canBeReplacedBy(goal)) {
                return false;
            }
        }

        return true;
    }

    /**
     * @reason Update our array instead
     * @author JellySquid
     */
    @Overwrite
    public void disableControl(Goal.Control control) {
        this.disabledControlsArray[control.ordinal()] = true;
    }

    /**
     * @reason Update our array instead
     * @author JellySquid
     */
    @Overwrite
    public void enableControl(Goal.Control control) {
        this.disabledControlsArray[control.ordinal()] = false;
    }

    private boolean isControlDisabled(Goal.Control control) {
        return this.disabledControlsArray[control.ordinal()];
    }

    private WeightedGoal getGoalForControl(Goal.Control control) {
        return this.goalsByControlArray[control.ordinal()];
    }

    private void setGoalForControl(Goal.Control control, WeightedGoal goal) {
        this.goalsByControlArray[control.ordinal()] = goal;
    }
}
