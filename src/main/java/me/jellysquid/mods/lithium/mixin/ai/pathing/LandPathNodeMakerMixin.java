package me.jellysquid.mods.lithium.mixin.ai.pathing;

import me.jellysquid.mods.lithium.common.ai.pathing.PathNodeCache;
import me.jellysquid.mods.lithium.common.ai.pathing.NodePosition;
import net.minecraft.entity.ai.pathing.BirdPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathContext;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockState;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BirdPathNodeMaker.class)
public abstract class LandPathNodeMakerMixin {

    @Inject(method = "getCommonNodeType",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/world/BlockView;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;",
                    shift = At.Shift.AFTER
            ),
            cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void getLithiumCachedCommonNodeType(BlockView world, BlockPos pos, CallbackInfoReturnable<PathNodeType> cir, BlockState blockState) {
        PathNodeType type = PathNodeCache.getPathNodeType(blockState);
        if (type != null) {
            cir.setReturnValue(type);
        }
    }

    @Inject(
            method = "getNodeTypeFromNeighbors", locals = LocalCapture.CAPTURE_FAILHARD,
            at = @At(
                    value = "INVOKE", shift = At.Shift.BEFORE,
                    target = "Lnet/minecraft/entity/ai/pathing/PathContext;getNodeType(III)Lnet/minecraft/entity/ai/pathing/PathNodeType;"
            ),
            cancellable = true
    )
    private static void doNotIteratePositionsIfLithiumSinglePosCall(PathContext context, int x, int y, int z, PathNodeType fallback, CallbackInfoReturnable<PathNodeType> cir, int i, int j, int k) {
        if (fallback == null) {
            if (i != -1 || j != -1 || k != -1) {
                cir.setReturnValue(null);
            }
        }
    }

    @Redirect(method = "getLandNodeType(Lnet/minecraft/entity/ai/pathing/PathContext;Lnet/minecraft/util/math/BlockPos$Mutable;)Lnet/minecraft/entity/ai/pathing/PathNodeType;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/ai/pathing/LandPathNodeMaker;getNodeTypeFromNeighbors(Lnet/minecraft/entity/ai/pathing/PathContext;IIILnet/minecraft/entity/ai/pathing/PathNodeType;)Lnet/minecraft/entity/ai/pathing/PathNodeType;"))
    private static PathNodeType getNodeTypeFromNeighbors(PathContext context, int x, int y, int z, PathNodeType fallback) {
        return PathNodeCache.getNodeTypeFromNeighbors(context, new NodePosition(x, y, z), fallback);
    }
}