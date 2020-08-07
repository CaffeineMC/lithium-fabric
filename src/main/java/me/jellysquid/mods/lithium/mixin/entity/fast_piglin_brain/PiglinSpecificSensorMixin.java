package me.jellysquid.mods.lithium.mixin.entity.fast_piglin_brain;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.PiglinSpecificSensor;

@Mixin(PiglinSpecificSensor.class)
public class PiglinSpecificSensorMixin {

    @Overwrite
    protected void sense(ServerWorld world, LivingEntity entity) {
        world.getProfiler().push("Piglin Sensing");

        Brain<?> brain = entity.getBrain();
        brain.remember(MemoryModuleType.NEAREST_REPELLENT, findSoulFire(world, entity));
        List<PiglinEntity> visableAdultPiglins = Lists.newArrayList();
        List<PiglinEntity> allAdultPiglins = Lists.newArrayList();

        // This list appears to be sorted from nearest to furthest
        List<LivingEntity> allVisibleMobs = (List)brain.getOptionalMemory(MemoryModuleType.VISIBLE_MOBS).orElse(ImmutableList.of());

        int visibleHoglinCount = 0;
        boolean isAdult;

        // prevent allocations and tons of calls each time through the loop
        boolean foundHuntableHoglin = false;
        boolean foundBabyHoglin = false;
        boolean foundBabyPiglin = false;
        boolean foundAttackablePlayer = false;
        boolean foundPlayerHoldingGold = false;
        boolean foundZombifiedPiglin = false;
        boolean foundNemisis = false;
        
        for(LivingEntity currentLivingEntity : allVisibleMobs) {
            // avoid many calls to isBaby() and just inline by hand to make sure

            isAdult = !currentLivingEntity.isBaby();

                // check if entity is a hoglin
                if(currentLivingEntity instanceof HoglinEntity)  {
                    if(isAdult) {
                        visibleHoglinCount++;
                        if(!foundHuntableHoglin && ((HoglinEntity)currentLivingEntity).canBeHunted()) {
                            brain.remember(MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, Optional.of((HoglinEntity)currentLivingEntity));
                            foundHuntableHoglin = true;
                        }
                    } else {
                        if(!foundBabyHoglin) {
                            brain.remember(MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, Optional.of((HoglinEntity)currentLivingEntity));
                        }
                    }
                    continue;
                }

                // check if entity is a piglin
                else if(currentLivingEntity instanceof PiglinEntity) {
                    if(isAdult) {
                        visableAdultPiglins.add((PiglinEntity)currentLivingEntity);
                    } else if(!foundBabyPiglin) {
                        brain.remember(MemoryModuleType.NEAREST_VISIBLE_BABY_PIGLIN, Optional.of((PiglinEntity)currentLivingEntity));
                    }
                    continue;
                }

                // check if entity is a player
                else if(currentLivingEntity instanceof PlayerEntity) {
                    if(!foundAttackablePlayer && EntityPredicates.EXCEPT_CREATIVE_SPECTATOR_OR_PEACEFUL.test(currentLivingEntity) && !PiglinBrain.wearsGoldArmor((PlayerEntity)currentLivingEntity)) {
                        brain.remember(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, (PlayerEntity)currentLivingEntity);
                        foundAttackablePlayer = true;
                    } else if(!foundPlayerHoldingGold && !((PlayerEntity)currentLivingEntity).isSpectator() && PiglinBrain.isGoldHoldingPlayer((PlayerEntity)currentLivingEntity)) {
                        brain.remember(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, (PlayerEntity)currentLivingEntity);
                        foundPlayerHoldingGold = true;
                    }
                    continue;
                }

                // check if entity is Zombified Piglin
                else if(currentLivingEntity instanceof ZombifiedPiglinEntity) {
                    if(!foundZombifiedPiglin) {
                        brain.remember(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, (ZombifiedPiglinEntity)currentLivingEntity);
                        foundZombifiedPiglin = true;
                    }
                }

                // check if entity can be a nemisis
                else if(currentLivingEntity instanceof WitherEntity || currentLivingEntity instanceof WitherEntity) {
                    if(!foundNemisis) {
                        brain.remember(MemoryModuleType.NEAREST_VISIBLE_NEMESIS, (MobEntity)currentLivingEntity);
                        foundNemisis = true;
                    }
                }
                
            }

            // finish adding memories
            brain.remember(MemoryModuleType.NEAREST_ADULT_PIGLINS, allAdultPiglins);
            brain.remember(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, allAdultPiglins.size());
            brain.remember(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, visibleHoglinCount);
            
        }
            
        

    @Shadow
    private static Optional<BlockPos> findSoulFire(ServerWorld world, LivingEntity entity) {
        return null;
    }
}