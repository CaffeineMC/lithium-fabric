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

import java.util.Set;
import java.util.function.Supplier;

@Mixin(GoalSelector.class)
public abstract class MixinGoalSelector {
    @Shadow
    @Final
    private Supplier<Profiler> profiler;

    @Mutable
    @Shadow
    @Final
    private Set<WeightedGoal> goals;

    /**
     * Array of flags indicating which controls are disabled for the entity, indexed by the control enum's ordinal. If
     * the value is true for a control, it is disabled.
     */
    private boolean[] disabledControlsArray;

    /**
     * Array of goals which have acquired a AI control, indexed by the control enum's ordinal. A null value indicates
     * that no goal has acquired the control.
     */
    private WeightedGoal[] goalsByControlArray;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(Supplier<Profiler> supplier, CallbackInfo ci) {
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
        this.updateGoalStates();
        this.tickGoals();
    }

    /**
     * Checks the state of all goals for the given entity, starting and stopping them as necessary (because a goal
     * has been disabled, the controls are no longer available or have been reassigned, etc.)
     */
    private void updateGoalStates() {
        this.profiler.get().push("goalUpdate");

        // Stop any goals which are disabled or shouldn't continue executing
        this.stopGoals();
        
        // Update the controls
        this.cleanupControls();

        // Try to start new goals where possible
        this.startGoals();

        this.profiler.get().pop();
    }

    /**
     * Attempts to stop all goals which are running and either shouldn't continue or no longer have available controls.
     */
    private void stopGoals() {
        for (WeightedGoal goal : this.goals) {
            // Filter out goals which are not running
            if (!goal.isRunning()) {
                continue;
            }

            // If the goal shouldn't continue or any of its controls have been disabled, then stop the goal
            if (!goal.shouldContinue() || this.areControlsDisabled(goal)) {
                goal.stop();
            }
        }
    }

    /**
     * Performs a scan over all currently held controls and releases them if their associated goal is stopped.
     */
    private void cleanupControls() {
        for (int i = 0; i < this.goalsByControlArray.length; i++) {
            WeightedGoal goal = this.goalsByControlArray[i];

            // If the control has been acquired by a goal, check if the goal should still be running
            // If the goal should not be running anymore, release the control held by it
            if (goal != null && !goal.isRunning()) {
                this.goalsByControlArray[i] = null;
            }
        }
    }

    /**
     * Attempts to start all goals which are not-already running, can be started, and have their controls available.
     */
    private void startGoals() {
        for (WeightedGoal goal : this.goals) {
            // Filter out goals which are already running or can't be started
            if (goal.isRunning() || !goal.canStart()) {
                continue;
            }

            // Check if the goal's controls are available or can be replaced
            if (!this.areGoalControlsAvailable(goal)) {
                continue;
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
    }

    /**
     * Ticks all running AI goals.
     */
    private void tickGoals() {
        this.profiler.get().push("goalTick");

        // Tick all currently running goals
        for (WeightedGoal goal : this.goals) {
            if (goal.isRunning()) {
                goal.tick();
            }
        }

        this.profiler.get().pop();
    }

    /**
     * Returns true if any controls of the specified goal are disabled.
     */
    private boolean areControlsDisabled(WeightedGoal goal) {
        for (Goal.Control control : getControls(goal)) {
            if (this.isControlDisabled(control)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if all controls for the specified goal are either available (not acquired by another goal) or replaceable
     * (acquired by another goal, but eligible for replacement) and not disabled for the entity.
     */
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

    /**
     * Returns true if the specified control is disabled.
     */
    private boolean isControlDisabled(Goal.Control control) {
        return this.disabledControlsArray[control.ordinal()];
    }

    /**
     * Returns the goal which is currently holding the specified control, or null if no goal is.
     */
    private WeightedGoal getGoalOccupyingControl(Goal.Control control) {
        return this.goalsByControlArray[control.ordinal()];
    }

    /**
     * Changes the goal which is currently holding onto a control.
     */
    private void setGoalOccupyingControl(Goal.Control control, WeightedGoal goal) {
        this.goalsByControlArray[control.ordinal()] = goal;
    }

    /**
     * Helper method which accesses the flat required controls array from a goal.
     */
    private static Goal.Control[] getControls(WeightedGoal goal) {
        return ((ExtendedGoal) goal.getGoal()).getRequiredControls();
    }
}
