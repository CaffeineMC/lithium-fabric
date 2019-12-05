package me.jellysquid.mods.lithium.mixin.voxelshape.fast_shape_vertex_merging;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.util.BooleanBiFunction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import me.jellysquid.mods.lithium.common.shape.IndirectListPair;
import me.jellysquid.mods.lithium.common.shape.IndirectListPairCache;

@Mixin(VoxelShapes.class)
public abstract class MixinVoxelShapes {
    @Inject(method = "matchesAnywhere(Lnet/minecraft/util/shape/VoxelShape;Lnet/minecraft/util/shape/VoxelShape;Lnet/minecraft/util/BooleanBiFunction;)Z",
    		at = @At(value = "INVOKE",
    			target = "Lnet/minecraft/util/shape/VoxelShapes;matchesAnywhere(Lnet/minecraft/util/shape/DoubleListPair;Lnet/minecraft/util/shape/DoubleListPair;Lnet/minecraft/util/shape/DoubleListPair;Lnet/minecraft/util/shape/VoxelSet;Lnet/minecraft/util/shape/VoxelSet;Lnet/minecraft/util/BooleanBiFunction;)Z"
    		), locals = LocalCapture.CAPTURE_FAILEXCEPTION) //Be very weary of this during updates, the final three arguments will match anything but primitives if the method changes
    private static void releaseListPairs(VoxelShape aShape, VoxelShape bShape, BooleanBiFunction func, CallbackInfoReturnable<Boolean> callback, boolean ab, boolean ba, @Coerce Object listX, @Coerce Object listY, @Coerce Object listZ) {
    	if (listX instanceof IndirectListPair) IndirectListPairCache.release((IndirectListPair) listX);
        if (listY instanceof IndirectListPair) IndirectListPairCache.release((IndirectListPair) listY);
        if (listZ instanceof IndirectListPair) IndirectListPairCache.release((IndirectListPair) listZ);
    }

    @Redirect(method = "createListPair", at = @At(value = "INVOKE", target = "Ljava/util/Objects;equals(Ljava/lang/Object;Ljava/lang/Object;)Z", remap = false))
    private static boolean betterEquals(Object left, Object right) {
    	DoubleList a = (DoubleList) left; //The method invoke loses the types, both will always be DoubleLists so this cast is safe enough
    	DoubleList b = (DoubleList) right;

    	//Checking a.size() == b.size() is already done by createListPair
    	int size = a.size();

        for (int i = 0; i < size; i++) {
            if (a.getDouble(i) != b.getDouble(i)) {
                return false;
            }
        }

        return true;
    }

    @Inject(method = "createListPair",
            at = @At(value = "NEW", target = "net/minecraft/util/shape/SimpleDoubleListPair"),
            cancellable = true)
	private static void betterSimpleList(int size, DoubleList a, DoubleList b, boolean includeA, boolean includeB, CallbackInfoReturnable<Object> callback) {
    	callback.setReturnValue(IndirectListPairCache.create(a, b, includeA, includeB)); //Little bit of naughtiness with the generics as we can't see the real return type
    }
}
