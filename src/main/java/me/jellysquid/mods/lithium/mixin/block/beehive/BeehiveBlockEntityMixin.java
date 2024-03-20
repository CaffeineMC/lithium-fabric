package me.jellysquid.mods.lithium.mixin.block.beehive;

import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(BeehiveBlockEntity.class)
public class BeehiveBlockEntityMixin {

    @Inject(
            method = "releaseBee",
            at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/nbt/NbtCompound;copy()Lnet/minecraft/nbt/NbtCompound;"),
            cancellable = true
    )
    private static void exitEarlyIfHiveObstructed(World world, BlockPos pos, BlockState state, @Coerce Object bee, @Nullable List<Entity> entities, BeehiveBlockEntity.BeeState beeState, @Nullable BlockPos flowerPos, CallbackInfoReturnable<Boolean> cir) {
        Direction direction = state.get(BeehiveBlock.FACING);
        BlockPos blockPos = pos.offset(direction);

        if (beeState != BeehiveBlockEntity.BeeState.EMERGENCY && !world.getBlockState(blockPos).getCollisionShape(world, blockPos).isEmpty()) {
            cir.setReturnValue(false);
        };
    }
}
