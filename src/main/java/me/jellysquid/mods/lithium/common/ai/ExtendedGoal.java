package me.jellysquid.mods.lithium.common.ai;

import net.minecraft.entity.ai.goal.Goal;

public interface ExtendedGoal {
    Goal.Control[] getRequiredControls();
}
