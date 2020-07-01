package me.jellysquid.mods.lithium.mixin.ai.task;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.FollowMobTask;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

@Mixin(FollowMobTask.class)
public class MixinFollowMobTask {
    @Shadow
    @Final
    private Predicate<LivingEntity> predicate;

    /**
     * @reason Replace stream code with traditional iteration
     * @author Maity
     */
    @Overwrite
    protected boolean shouldRun(ServerWorld world, LivingEntity entity) {
        final Brain<?> brain = entity.getBrain();

        final List<LivingEntity> visibleMobs = brain.getOptionalMemory(MemoryModuleType.VISIBLE_MOBS)
                .orElse(Collections.emptyList());

        for (LivingEntity mob : visibleMobs) {
            if (this.predicate.test(mob)) {
                return true;
            }
        }

        return false;
    }
}
