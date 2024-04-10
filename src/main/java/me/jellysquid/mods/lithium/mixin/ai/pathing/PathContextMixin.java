package me.jellysquid.mods.lithium.mixin.ai.pathing;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.ai.pathing.PathContext;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PathContext.class)
public class PathContextMixin {

    @WrapOperation(
            method = "<init>",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;getWorld()Lnet/minecraft/world/World;")
    )
    private World getWorldIfNonNull(MobEntity instance, Operation<World> original) {
        if (instance == null) {
            return null;
        }
        return original.call(instance);
    }

    @WrapOperation(
            method = "<init>",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;getBlockPos()Lnet/minecraft/util/math/BlockPos;")
    )
    private BlockPos getBlockPosIfNonNull(MobEntity instance, Operation<BlockPos> original) {
        if (instance == null) {
            return null;
        }
        return original.call(instance);
    }
}
