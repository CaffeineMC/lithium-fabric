package me.jellysquid.mods.lithium.mixin.ai.task;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.*;
import net.minecraft.entity.ai.brain.task.FindEntityTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@Mixin(FindEntityTask.class)
public abstract class FindEntityTaskMixin<E extends LivingEntity, T extends LivingEntity> extends Task<E>  {
    @Shadow
    @Final
    private int completionRange;
    @Shadow
    @Final
    private float speed;
    @Shadow
    @Final
    private EntityType<? extends T> entityType;
    @Shadow
    @Final
    private int maxSquaredDistance;
    @Shadow
    @Final
    private Predicate<T> predicate;
    @Shadow
    @Final
    private MemoryModuleType<T> targetModule;

    private FindEntityTaskMixin(Map<MemoryModuleType<?>, MemoryModuleState> requiredMemoryState) {
        super(requiredMemoryState);
    }

    @Shadow
    public abstract boolean method_24583(LivingEntity livingEntity);

    /**
     * @reason Replace stream code with traditional iteration
     * @author Maity
     */
    @Overwrite
    private boolean method_24582(E livingEntity) {
        List<LivingEntity> visibleMobs = livingEntity.getBrain().getOptionalMemory(MemoryModuleType.VISIBLE_MOBS)
                .orElse(Collections.emptyList());

        for (LivingEntity mob : visibleMobs) {
            if (this.method_24583(mob)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @reason Replace stream code with traditional iteration
     * @author Maity
     */
    @Overwrite
    public void run(ServerWorld world, E self, long time) {
        Brain<?> brain = self.getBrain();

        List<LivingEntity> visibleMobs = brain.getOptionalMemory(MemoryModuleType.VISIBLE_MOBS)
                .orElse(Collections.emptyList());

        for (LivingEntity mob : visibleMobs) {
            if (!this.entityType.equals(mob.getType())) {
                continue;
            }

            if (mob.squaredDistanceTo(self) > (double) this.maxSquaredDistance) {
                continue;
            }

            //noinspection unchecked
            if (this.predicate.test((T) mob)) {
                // [VanillaCopy]
                //noinspection unchecked
                brain.remember(this.targetModule, Optional.of((T) mob));
                brain.remember(MemoryModuleType.LOOK_TARGET, new EntityLookTarget(mob, true));
                brain.remember(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityLookTarget(mob, false), this.speed, this.completionRange));

                break;
            }
        }
    }
}
