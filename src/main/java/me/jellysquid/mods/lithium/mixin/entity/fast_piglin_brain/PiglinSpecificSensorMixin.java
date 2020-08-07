package me.jellysquid.mods.lithium.mixin.entity.fast_piglin_brain;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
    public void sense(ServerWorld world, LivingEntity entity) {
        Brain<?> brain = entity.getBrain();
        brain.remember(MemoryModuleType.NEAREST_REPELLENT, findSoulFire(world, entity));
        List<PiglinEntity> visableAdultPiglins = Lists.newArrayList();

        // This list appears to be sorted from nearest to furthest
        List<LivingEntity> allVisibleMobs = (List)brain.getOptionalMemory(MemoryModuleType.VISIBLE_MOBS).orElse(ImmutableList.of());

        int visibleHoglinCount = 0;

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

            if(currentLivingEntity.isBaby()) {
                // check for hoglins
                if(!foundBabyHoglin && currentLivingEntity instanceof HoglinEntity)  {
                    brain.remember(MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, Optional.of((HoglinEntity)currentLivingEntity));
                    continue;
                }

                else if(!foundBabyPiglin && currentLivingEntity instanceof PiglinEntity) {
                    brain.remember(MemoryModuleType.NEAREST_VISIBLE_BABY_PIGLIN, Optional.of((PiglinEntity)currentLivingEntity));
                    continue;
                }
            } else {

                if(currentLivingEntity instanceof PiglinEntity) {
                    visableAdultPiglins.add((PiglinEntity)currentLivingEntity);
                    continue;

                } else if(currentLivingEntity instanceof HoglinEntity)  {
                    visibleHoglinCount++;
                    if(!foundHuntableHoglin && ((HoglinEntity)currentLivingEntity).canBeHunted()) {
                        brain.remember(MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, Optional.of((HoglinEntity)currentLivingEntity));
                        foundHuntableHoglin = true;
                    }
                    continue;
                }
                
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

                // check if entity is zombified piglin
                else if(!foundZombifiedPiglin && currentLivingEntity instanceof ZombifiedPiglinEntity) {
                    brain.remember(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, (ZombifiedPiglinEntity)currentLivingEntity);
                    foundZombifiedPiglin = true;
                    continue;
                }

                // check if entity can be a nemisis
                else if(!foundNemisis && currentLivingEntity instanceof WitherEntity || currentLivingEntity instanceof WitherEntity) {
                    brain.remember(MemoryModuleType.NEAREST_VISIBLE_NEMESIS, (MobEntity)currentLivingEntity);
                    foundNemisis = true;
                    continue;
                }


            }
            
        }

        // finish adding memories
        brain.remember(MemoryModuleType.NEAREST_ADULT_PIGLINS, visableAdultPiglins);
        brain.remember(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, visableAdultPiglins.size());
        brain.remember(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, visibleHoglinCount);
    }
            
    @Shadow
    private static Optional<BlockPos> findSoulFire(ServerWorld world, LivingEntity entity) {
        return null;
    }
}