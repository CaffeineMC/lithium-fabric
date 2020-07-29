package me.jellysquid.mods.lithium.mixin.entity.fast_piglin_brain;

import java.util.List;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.GameRules;

@Mixin(PiglinBrain.class)
public class PiglinBrainMixin {

    /**
     * The vanilla implimentation here seems to be heavy usage of
     * functional style java and similar which performs poorly when
     * used in hot code
     * 
     * This optimization here helps to reduce a spike in lag from 
     * breaking a specific block when near a bunch of piglins
     * 
     * @param player the player that broke the block
     * @param isInventory if the player broke a block that stores items 
     */
    @Inject(method = "onGuardedBlockBroken", at = @At("HEAD"), cancellable = true)
    private static void onGuardedBlockBroken(PlayerEntity player, boolean isInventory, CallbackInfo ci) {
        List<PiglinEntity> list = player.world.getNonSpectatingEntities(PiglinEntity.class, player.getBoundingBox().expand(16.0D));
        PlayerEntity target;

        for(PiglinEntity current : list) {
            if(current.getBrain().hasActivity(Activity.IDLE) && !isInventory && LookTargetUtil.isVisibleInMemory(current, player)) {
                target = current.world.getGameRules().getBoolean(GameRules.UNIVERSAL_ANGER) ? getNearestDetectedPlayer(current).get() : player;
                becomeAngryWith(current, target);
            }
        }

        ci.cancel();
    }

    /**
     * This is quite similar to the first method but would be
     * called quite a bit more often than the other with calls at
     * least every single time a player attacks a piglin
     * @param piglin
     */
    @Inject(method = "angerNearbyPiglins", at = @At("HEAD"), cancellable = true)
    private static void angerNearbyPiglins(PiglinEntity piglin, CallbackInfo ci) {
        List<PiglinEntity> nearPiglins = getNearbyPiglins(piglin);
        Optional<PlayerEntity> player;

        for(PiglinEntity current : nearPiglins) {
            player = getNearestDetectedPlayer(current);
            if(player.isPresent()) {
                becomeAngryWith(current, player.get());
            }
        }

        ci.cancel();
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