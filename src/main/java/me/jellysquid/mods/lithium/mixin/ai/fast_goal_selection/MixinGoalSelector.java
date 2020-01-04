package me.jellysquid.mods.lithium.mixin.ai.fast_goal_selection;

import me.jellysquid.mods.lithium.common.ai.ExtendedGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.WeightedGoal;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

@Mixin(GoalSelector.class)
public abstract class MixinGoalSelector {
    @Shadow
    @Final
    private Profiler profiler;

    @Mutable
    @Shadow
    @Final
    private Set<WeightedGoal> goals;

    private boolean[] disabledControlsArray;

    private WeightedGoal[] goalsByControlArray;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(Profiler profiler, CallbackInfo ci) {
        this.disabledControlsArray = new boolean[4];
        this.goalsByControlArray = new WeightedGoal[4];

        this.goals = new HashSet<>();
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
        this.modifyGoals();
        this.tickGoals();
    }

    private void modifyGoals() {
        this.profiler.push("goalUpdate");

        // Stop any goals which are disabled or shouldn't continue executing
        this.stopGoals();
        
        // Update the controls
        this.updateControls();

        // Try to start new goals where possible
        this.startGoals();

        this.profiler.pop();
    }

    private void stopGoals() {
        for (WeightedGoal goal : this.goals) {
            // Filter out goals which are not running
            if (!goal.isRunning()) {
                continue;
            }

            // If the goal shouldn't continue or any of its controls have been disabled, then stop the goal
            if (this.areControlsDisabled(goal) || !goal.shouldContinue()) {
                goal.stop();
            }
        }
    }

    private void updateControls() {
        // Disassociate controls from stopped goals
        for (int i = 0; i < this.goalsByControlArray.length; i++) {
            WeightedGoal goal = this.goalsByControlArray[i];

            if (goal != null && !goal.isRunning()) {
                this.goalsByControlArray[i] = null;
            }
        }
    }
    
    private void startGoals() {
        for (WeightedGoal goal : this.goals) {
            // Filter out goals which can't be started
            if (goal.isRunning()) {
                continue;
            }

            this.attemptGoalStart(goal);
        }
    }

    private void attemptGoalStart(WeightedGoal goal) {
        if (!goal.canStart()) {
            return;
        }

        // Check if the goal's controls are available or can be replaced
        if (!this.areGoalControlsAvailable(goal)) {
            return;
        }

        // Hand over controls to this goal and stop any goals which depended on those controls
        for (Goal.Control control : getControls(goal)) {
            WeightedGoal otherGoal = this.getGoalOccupyingControl(control);

            if (otherGoal != null) {
                otherGoal.stop();
            }

            this.setGoalOccupyingControl(control, goal);
        }

        goal.start();
    }

    private void tickGoals() {
        this.profiler.push("goalTick");

        // Tick all currently running goals
        for (WeightedGoal goal : this.goals) {
            if (goal.isRunning()) {
                goal.tick();
            }
        }

        this.profiler.pop();
    }

    private boolean areControlsDisabled(WeightedGoal goal) {
        for (Goal.Control control : getControls(goal)) {
            if (this.isControlDisabled(control)) {
                return true;
            }
        }

        return false;
    }

    private boolean areGoalControlsAvailable(WeightedGoal goal) {
        for (Goal.Control control : getControls(goal)) {
            if (this.isControlDisabled(control)) {
                return false;
            }

            WeightedGoal occupied = this.getGoalOccupyingControl(control);

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

    private WeightedGoal getGoalOccupyingControl(Goal.Control control) {
        return this.goalsByControlArray[control.ordinal()];
    }

    private void setGoalOccupyingControl(Goal.Control control, WeightedGoal goal) {
        this.goalsByControlArray[control.ordinal()] = goal;
    }

    private static Goal.Control[] getControls(WeightedGoal goal) {
        return ((ExtendedGoal) goal.getGoal()).getRequiredControls();
    }
}
