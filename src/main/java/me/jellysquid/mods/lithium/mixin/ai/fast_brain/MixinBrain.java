package me.jellysquid.mods.lithium.mixin.ai.fast_brain;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;
import java.util.Set;

@Mixin(Brain.class)
public abstract class MixinBrain<E extends LivingEntity> {
    @Shadow
    @Final
    private Map<SensorType<? extends Sensor<? super E>>, Sensor<? super E>> sensors;

    @Shadow
    @Final
    private Map<Integer, Map<Activity, Set<Task<? super E>>>> tasks;

    @Shadow
    @Final
    private Set<Activity> possibleActivities;

    /**
     * @reason Replace stream-based code with traditional iteration
     * @author JellySquid
     */
    @Overwrite
    private void updateSensors(ServerWorld world, E entity) {
        for (Sensor<? super E> sensor : this.sensors.values()) {
            sensor.canSense(world, entity);
        }
    }

    /**
     * @reason Replace stream-based code with traditional iteration
     * @author JellySquid
     */
    @Overwrite
    private void startTasks(ServerWorld world, E entity) {
        long time = world.getTime();

        for (Map<Activity, Set<Task<? super E>>> map : this.tasks.values()) {
            for (Map.Entry<Activity, Set<Task<? super E>>> entry : map.entrySet()) {
                if (!this.possibleActivities.contains(entry.getKey())) {
                    continue;
                }

                for (Task<? super E> task : entry.getValue()) {
                    if (task.getStatus() == Task.Status.STOPPED) {
                        task.tryStarting(world, entity, time);
                    }
                }
            }
        }
    }

    /**
     * @reason Replace stream-based code with traditional iteration
     * @author JellySquid
     */
    @Overwrite
    private void updateTasks(ServerWorld world, E entity) {
        long time = world.getTime();

        for (Map<Activity, Set<Task<? super E>>> map : this.tasks.values()) {
            for (Map.Entry<Activity, Set<Task<? super E>>> entry : map.entrySet()) {
                for (Task<? super E> task : entry.getValue()) {
                    if (task.getStatus() == Task.Status.RUNNING) {
                        task.tick(world, entity, time);
                    }
                }
            }
        }
    }
}
