package me.jellysquid.mods.lithium.mixin.voxelshape.fast_vertex_merging;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import me.jellysquid.mods.lithium.common.shapes.pairs.LithiumDoublePairList;
import net.minecraft.util.shape.PairList;
import net.minecraft.util.shape.VoxelShapes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VoxelShapes.class)
public class MixinVoxelShapes {
    @Inject(method = "createListPair", at = @At(value = "NEW", target = "net/minecraft/util/shape/SimplePairList", shift = At.Shift.BEFORE), cancellable = true)
    private static void injectCustomListPair(int size, DoubleList a, DoubleList b, boolean flag1, boolean flag2, CallbackInfoReturnable<PairList> cir) {
        cir.setReturnValue(new LithiumDoublePairList(a, b, flag1, flag2));
    }
}
