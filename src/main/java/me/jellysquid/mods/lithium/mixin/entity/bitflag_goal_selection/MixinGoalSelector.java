package me.jellysquid.mods.lithium.mixin.entity.bitflag_goal_selection;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.WeightedGoal;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Mixin(GoalSelector.class)
public abstract class MixinGoalSelector {
    @Shadow
    @Final
    private Profiler profiler;

    @Shadow
    @Final
    private Map<Goal.Control, WeightedGoal> goalsByControl;

    @Shadow
    @Final
    private EnumSet<Goal.Control> disabledControls;

    private int disabledControlsBitFlag;

    @Shadow
    @Final
    private Set<WeightedGoal> goals;

    @Shadow
    @Final
    private static WeightedGoal activeGoal;

    /**
     * This method originally allocates a ton of objects due to the use of lambdas and streams. It also isn't
     * particularly fast while doing so. This performs significantly better.
     *
     * @author JellySquid
     */
    @Overwrite
    public void tick() {
        this.profiler.push("goalCleanup");

        this.disabledControlsBitFlag = 0;

        for (Goal.Control control : this.disabledControls) {
            this.disabledControlsBitFlag |= (1 << control.ordinal());
        }

        // Stop goals which are disabled
        for (WeightedGoal goal : this.goals) {
            if (!goal.isRunning()) {
                continue;
            }

            if (goal.shouldContinue() && !this.anyControlsDisabled(goal)) {
                continue;
            }

            goal.stop();
        }

        for (Map.Entry<Goal.Control, WeightedGoal> entry : this.goalsByControl.entrySet()) {
            if (!entry.getValue().isRunning()) {
                this.goalsByControl.remove(entry.getKey());
            }
        }

        this.profiler.pop();
        this.profiler.push("goalUpdate");

        for (WeightedGoal goal : this.goals) {
            if (goal.isRunning() || !goal.canStart()) {
                continue;
            }

            if (!this.areControlsAvailable(goal)) {
                return;
            }

            for (Goal.Control control : goal.getControls()) {
                WeightedGoal otherGoal = this.goalsByControl.getOrDefault(control, activeGoal);
                otherGoal.stop();

                this.goalsByControl.put(control, goal);
            }

            goal.start();
        }

        this.profiler.pop();
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
            if ((this.disabledControlsBitFlag & (1 << control.ordinal())) != 0) {
                return true;
            }
        }

        return false;
    }

    private boolean areControlsAvailable(WeightedGoal goal) {
        for (Goal.Control control : goal.getControls()) {
            if ((this.disabledControlsBitFlag & (1 << control.ordinal())) != 0) {
                return false;
            }

            if (!this.goalsByControl.getOrDefault(control, activeGoal).canBeReplacedBy(goal)) {
                return false;
            }
        }

        return true;
    }
}
