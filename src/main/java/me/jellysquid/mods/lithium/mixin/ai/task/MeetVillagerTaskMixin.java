package me.jellysquid.mods.lithium.mixin.ai.task;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.MeetVillagerTask;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.dynamic.GlobalPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Mixin(MeetVillagerTask.class)
public class MeetVillagerTaskMixin {

    /**
     * @reason Replace stream code with traditional iteration
     * @author Maity
     */
    @Overwrite
    public boolean shouldRun(ServerWorld world, LivingEntity entity) {
        Brain<?> brain = entity.getBrain();

        Optional<GlobalPos> optional = brain.getOptionalMemory(MemoryModuleType.MEETING_POINT);

        // [VanillaCopy]
        if (world.getRandom().nextInt(100) == 0
                && optional.isPresent() && world.getRegistryKey() == optional.get().getDimension()
                && optional.get().getPos().isWithinDistance(entity.getPos(), 4.0D)) {

            List<LivingEntity> visibleMobs = brain.getOptionalMemory(MemoryModuleType.VISIBLE_MOBS)
                    .orElse(Collections.emptyList());

            for (LivingEntity mob : visibleMobs) {
                if (EntityType.VILLAGER.equals(mob.getType())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * @reason Replace stream code with traditional iteration
     * @author Maity
     */
    @Overwrite
    public void run(ServerWorld world, LivingEntity entity, long time) {
        Brain<?> brain = entity.getBrain();

        List<LivingEntity> visibleMobs = brain.getOptionalMemory(MemoryModuleType.VISIBLE_MOBS)
                .orElse(Collections.emptyList());

        for (LivingEntity mob : visibleMobs) {
            if (!EntityType.VILLAGER.equals(mob.getType())) {
                continue;
            }

            if (mob.squaredDistanceTo(entity) <= 32.0D) {
                // [VanillaCopy]
                brain.remember(MemoryModuleType.INTERACTION_TARGET, mob);
                brain.remember(MemoryModuleType.LOOK_TARGET, new EntityLookTarget(mob, true));
                brain.remember(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityLookTarget(mob, false), 0.3F, 1));
                return;
            }
        }
    }
}
