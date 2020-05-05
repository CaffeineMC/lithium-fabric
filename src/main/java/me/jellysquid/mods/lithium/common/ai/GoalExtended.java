package me.jellysquid.mods.lithium.common.ai;

import net.minecraft.entity.ai.goal.Goal;

public interface GoalExtended {
    /**
     * Returns a flat array of the controls used by this goal.
     */
    Goal.Control[] getRequiredControls();
}
