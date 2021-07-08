package me.jellysquid.mods.lithium.mixin.ai.task.goat_jump;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import net.minecraft.entity.ai.brain.task.LongJumpTask;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Mixin(LongJumpTask.class)
public abstract class LongJumpTaskMixin {


    @Shadow
    @Final
    private static int MAX_COOLDOWN;
    private final LongArrayList potentialTargets = new LongArrayList();
    private final ShortArrayList potentialWeights = new ShortArrayList();
    @Shadow
    private Optional<LongJumpTask.Target> lastTarget;
    @Shadow
    private int cooldown;
    @Shadow
    @Final
    private List<LongJumpTask.Target> targets;
    @Shadow
    private Optional<Vec3d> lastPos;
    @Shadow
    @Final
    private int horizontalRange;
    @Shadow
    @Final
    private int verticalRange;

    private static int findIndex(ShortArrayList weights, int weightedIndex) {
        for (int i = 0; i < weights.size(); i++) {
            weightedIndex -= weights.getShort(i);
            if (weightedIndex < 0) {
                return i;
            }
        }
        return -1;
    }

    @Shadow
    protected abstract Optional<Vec3d> getRammingVelocity(MobEntity entity, Vec3d pos);

    /**
     * @author 2No2Name
     * @reason only evaluate 20+ instead of ~100 possible jumps without affecting behavior
     * [VanillaCopy] the whole method, commented changes
     */
    @Overwrite
    public void run(ServerWorld serverWorld, MobEntity mobEntity, long l) {
        this.potentialTargets.clear();
        this.potentialWeights.clear();
        int potentialTotalWeight = 0;

        this.lastTarget = Optional.empty();
        this.cooldown = MAX_COOLDOWN;
        this.targets.clear();
        this.lastPos = Optional.of(mobEntity.getPos());
        BlockPos goatPos = mobEntity.getBlockPos();
        int goatX = goatPos.getX();
        int goatY = goatPos.getY();
        int goatZ = goatPos.getZ();
        Iterable<BlockPos> iterable = BlockPos.iterate(goatX - this.horizontalRange, goatY - this.verticalRange, goatZ - this.horizontalRange, goatX + this.horizontalRange, goatY + this.verticalRange, goatZ + this.horizontalRange);
        EntityNavigation entityNavigation = mobEntity.getNavigation();

        BlockPos.Mutable targetPosCopy = new BlockPos.Mutable();
        for (BlockPos targetPos : iterable) {
            if (goatX == targetPos.getX() && goatZ == targetPos.getZ()) {
                continue;
            }
            double squaredDistance = targetPos.getSquaredDistance(goatPos);

            //Optimization: Evaluate the flight path check later (after random selection, but before world can be modified)
            if (entityNavigation.isValidPosition(targetPos) && mobEntity.getPathfindingPenalty(LandPathNodeMaker.getLandNodeType(mobEntity.world, targetPosCopy.set(targetPos))) == 0.0F) {
                this.potentialTargets.add(targetPos.asLong());
                int weight = MathHelper.ceil(squaredDistance);
                this.potentialWeights.add((short) weight);
                potentialTotalWeight += weight;
            }
        }
        //Optimization: Do the random picking of positions before doing the expensive the jump flight path validity check.
        //up to MAX_COOLDOWN random targets can be selected in keepRunning, so only this number of targets needs to be generated
        while (this.targets.size() < MAX_COOLDOWN) {
            //the number of random calls will be different from vanilla, but this is not reasonably detectable (not affecting world generation)
            if (potentialTotalWeight == 0) {
                return; //collection is empty/fully consumed, no more possible targets available
            }
            int chosenIndex = findIndex(this.potentialWeights, serverWorld.random.nextInt(potentialTotalWeight));
            long chosenPos = this.potentialTargets.getLong(chosenIndex);
            short chosenWeight = this.potentialWeights.set(chosenIndex, (short) 0);
            potentialTotalWeight -= chosenWeight;
            //Very expensive method call, it shifts bounding boxes around and checks for collisions with them
            Optional<Vec3d> optional = this.getRammingVelocity(mobEntity, Vec3d.ofCenter(targetPosCopy.set(chosenPos)));
            if (optional.isPresent()) {
                //the weight in Target should be unused, as the random selection already took place
                this.targets.add(new LongJumpTask.Target(new BlockPos(targetPosCopy), optional.get(), chosenWeight));
            }
        }
    }

    /**
     * Gets rid of the random selection of a target, as the targets have already been carefully randomly selected.
     */
    @Redirect(method = "keepRunning", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/WeightedPicker;getRandom(Ljava/util/Random;Ljava/util/List;)Ljava/util/Optional;"))
    private Optional<LongJumpTask.Target> getNextRandomTarget(Random random, List<LongJumpTask.Target> list) {
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(list.get(0));
    }
}




