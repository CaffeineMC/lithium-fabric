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

    /** A note to maintainers : 
      * These id's are not safe between versions and most likely will change if a new mob is added,
      * they are based on the order of entities being added to the ENTITY_TYPE registry
      * You can check a value by getting the output of Registry.ENTITY_TYPE.get(int) 
      * There is most likely a better way of doing this specifically however, static finals are inlined
      * every time they are used and in a loop, inlining a check of a registry numeric id of a string isn't
      * exactly amazing for performance.
      * 
      * If you are updating, check this code to make sure it is the expected entites being returned by those
      * numeric id's.
      * 
      * If a new mob is added to this sensor then its ID should be added here and the accoring code added.
    */
    private static final int HOGLIN_ID = 33;
    private static final int PIGLIN_ID = 60;
    private static final int PLAYER_ID = 105;
    private static final int ZOMBIFIED_ID = 104;


    @Overwrite
    protected void sense(ServerWorld world, LivingEntity entity) {
        Brain<?> brain = entity.getBrain();
        brain.remember(MemoryModuleType.NEAREST_REPELLENT, findSoulFire(world, entity));
        Optional<MobEntity> nearestVisibleNemisis = Optional.empty();
        Optional<HoglinEntity> nearestVisibleHuntableHoglin = Optional.empty();
        Optional<HoglinEntity> nearestVisibleBabyHoglin = Optional.empty();
        Optional<PiglinEntity> nearestVisibleBabyPiglin = Optional.empty();
        Optional<LivingEntity> nearestVisibleZombified = Optional.empty();
        Optional<PlayerEntity> nearPlayerNotInGold = Optional.empty();
        Optional<PlayerEntity> playerHoldingWantedItem = Optional.empty();
        List<PiglinEntity> visableAdultPiglins = Lists.newArrayList();
        List<PiglinEntity> allAdultPiglins = Lists.newArrayList();

        // This list appears to be sorted from nearest to furthest
        List<LivingEntity> allVisibleMobs = (List)brain.getOptionalMemory(MemoryModuleType.VISIBLE_MOBS).orElse(ImmutableList.of());
        Iterator visibleMobsIterator = allVisibleMobs.iterator();

        Stream players = allVisibleMobs.stream().filter(EntityPredicates.EXCEPT_CREATIVE_SPECTATOR_OR_PEACEFUL);
        Stream hoglins = allVisibleMobs.stream().filter((currentEntity) -> currentEntity.getEntityId() == HOGLIN_ID);
        Stream piglins = allVisibleMobs.stream().filter((currentEntity) -> currentEntity.getEntityId() == PIGLIN_ID);
        
        brain.remember(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, allVisibleMobs.stream().filter((currentEntity) -> currentEntity.getEntityId()==ZOMBIFIED_ID).findFirst());

        int visibleHoglinCount = 0;
        boolean isAdult;

        // prevent allocations and tons of calls each time through the loop
        boolean foundHuntableHoglin = false;
        boolean foundBabyHoglin = false;
        boolean foundBabyPiglin = false;
        boolean foundAttackablePlayer = false;
        boolean foundPlayerHoldingGold = false;
        boolean foundNemisis = false;
        
        for(LivingEntity currentLivingEntity : allVisibleMobs) {
            // avoid many calls to isBaby() and just inline by hand to make sure
            isAdult = !currentLivingEntity.isBaby();

            /**
             * To my knowledge, switches are faster than multiple if's.
             * Also checking an int comparison in this case should be quicker than
             * checking an instanceof, this should also be a bit more optimized for 
             * the order at which things can be dectected to do early stopping in
             * the switch statement due to how the fallthrough works.
             * 
             * To avoid more allocations, use casting as opposed to more variables
             * and do so only when necessary
             */
            
            switch(currentLivingEntity.getEntityId()){

                // check if entity is a hoglin
                case HOGLIN_ID : {
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
                    break;
                }

                // check if entity is a piglin
                case PIGLIN_ID : {
                    if(isAdult) {
                        visableAdultPiglins.add((PiglinEntity)currentLivingEntity);
                    } else if(!foundBabyPiglin) {
                        
                    }
                }
            }

            // stop iterating if all required targets were found
            if(foundAttackablePlayer && foundBabyHoglin && foundBabyPiglin && foundHuntableHoglin && foundNemisis && foundPlayerHoldingGold)
                return;
        }

        while(visibleMobsIterator.hasNext()) {
            LivingEntity currentLivingEntity = (LivingEntity)visibleMobsIterator.next();

            // Check hoglins
            if (currentLivingEntity instanceof HoglinEntity) {
                HoglinEntity hoglinEntity = (HoglinEntity)currentLivingEntity;
                if (!nearestVisibleBabyHoglin.isPresent() && hoglinEntity.isBaby()) {
                    nearestVisibleBabyHoglin = Optional.of(hoglinEntity);
                } else if (hoglinEntity.isAdult()) {
                    ++visibleHoglinCount;
                    if (!nearestVisibleHuntableHoglin.isPresent() && hoglinEntity.canBeHunted()) {
                        nearestVisibleHuntableHoglin = Optional.of(hoglinEntity);
                    }
                }

            // check piglins
            } else if (currentLivingEntity instanceof PiglinEntity) {
                PiglinEntity piglinEntity = (PiglinEntity)currentLivingEntity;
                
                // check babies
                if (piglinEntity.isBaby() && !nearestVisibleBabyPiglin.isPresent()) {
                    nearestVisibleBabyPiglin = Optional.of(piglinEntity);
                // check adults
                } else if (piglinEntity.isAdult()) {
                    visableAdultPiglins.add(piglinEntity);
                }

            // check players
            } else if (currentLivingEntity instanceof PlayerEntity) {
                PlayerEntity playerEntity = (PlayerEntity)currentLivingEntity;
                //check player thats not in gold
                if (!nearPlayerNotInGold.isPresent() && EntityPredicates.EXCEPT_CREATIVE_SPECTATOR_OR_PEACEFUL.test(currentLivingEntity) && !PiglinBrain.wearsGoldArmor(playerEntity)) {
                    nearPlayerNotInGold = Optional.of(playerEntity);
                }

                // check player holding wanted item
                if (!playerHoldingWantedItem.isPresent() && !playerEntity.isSpectator() && PiglinBrain.isGoldHoldingPlayer(playerEntity)) {
                    playerHoldingWantedItem = Optional.of(playerEntity);
                }

            // check for nemisis
            } else if (nearestVisibleNemisis.isPresent() || !(currentLivingEntity instanceof WitherSkeletonEntity) && !(currentLivingEntity instanceof WitherEntity)) {
                if (!nearestVisibleZombified.isPresent() && PiglinBrain.isZombified(currentLivingEntity.getType())) {
                    nearestVisibleZombified = Optional.of(currentLivingEntity);
                }
            } else {
                nearestVisibleNemisis = Optional.of((MobEntity)currentLivingEntity);
            }
        }

        List<LivingEntity> mobsMemory = (List)brain.getOptionalMemory(MemoryModuleType.MOBS).orElse(ImmutableList.of());
        Iterator mobMemoryIterator = mobsMemory.iterator();

        while(mobMemoryIterator.hasNext()) {
            LivingEntity currentMobInMemory = (LivingEntity)mobMemoryIterator.next();
            if (currentMobInMemory instanceof PiglinEntity && ((PiglinEntity)currentMobInMemory).isAdult()) {
                allAdultPiglins.add((PiglinEntity)currentMobInMemory);
            }
        }

        brain.remember(MemoryModuleType.NEAREST_VISIBLE_NEMESIS, nearestVisibleNemisis);
        brain.remember(MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, nearestVisibleHuntableHoglin);
        brain.remember(MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, nearestVisibleBabyHoglin);
        brain.remember(MemoryModuleType.NEAREST_VISIBLE_BABY_PIGLIN, nearestVisibleBabyPiglin);
        brain.remember(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, nearestVisibleZombified);
        brain.remember(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, nearPlayerNotInGold);
        brain.remember(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, playerHoldingWantedItem);
        brain.remember(MemoryModuleType.NEAREST_ADULT_PIGLINS, allAdultPiglins);
        brain.remember(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS, visableAdultPiglins);
        brain.remember(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, visableAdultPiglins.size());
        brain.remember(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, visibleHoglinCount);
        return;
        }
            
        

    @Shadow
    private static Optional<BlockPos> findSoulFire(ServerWorld world, LivingEntity entity) {
        return null;
    }
}