package me.jellysquid.mods.lithium.common.ai;

import net.minecraft.entity.ai.goal.Goal;

public interface GoalExtended {
    Goal.Control[] getRequiredControls();
}
