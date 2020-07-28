package me.jellysquid.mods.lithium.mixin.entity.fast_piglin_brain;

import java.util.List;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.GameRules;

@Mixin(PiglinBrain.class)
public class PiglinBrainMixin {
    @Overwrite
    public static void onGuardedBlockBroken(PlayerEntity player, boolean isInventory) {
        List<PiglinEntity> list = player.world.getNonSpectatingEntities(PiglinEntity.class, player.getBoundingBox().expand(16.0D));

        PlayerEntity target;
        for(PiglinEntity current : list) {
            if(current.getBrain().hasActivity(Activity.IDLE) && !isInventory && LookTargetUtil.isVisibleInMemory(current, player)) {
                target = current.world.getGameRules().getBoolean(GameRules.UNIVERSAL_ANGER) ? getNearestDetectedPlayer(current).get() : player;
                becomeAngryWith(current, target);
            }
        }
    }

    @Overwrite
    public static void angerNearbyPiglins(PiglinEntity piglin) {
        List<PiglinEntity> nearPiglins = getNearbyPiglins(piglin);

        Optional<PlayerEntity> player;
        for(PiglinEntity current : nearPiglins) {
            player = getNearestDetectedPlayer(current);
            if(player.isPresent()) {
                becomeAngryWith(current, player.get());
            }
        }
    }

    @Shadow
    private static List<PiglinEntity> getNearbyPiglins(PiglinEntity piglin) {
        return null;
    }

    @Shadow
    private static Optional<PlayerEntity> getNearestDetectedPlayer(PiglinEntity current) {
        return null;
    }

    @Shadow
    protected static void becomeAngryWith(PiglinEntity piglin, LivingEntity target) {
    }

    @Shadow
    protected static boolean hasIdleActivity(PiglinEntity piglin) {
        return true;
    }
}