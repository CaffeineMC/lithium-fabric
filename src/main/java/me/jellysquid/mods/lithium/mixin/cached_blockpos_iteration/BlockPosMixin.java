package me.jellysquid.mods.lithium.mixin.cached_blockpos_iteration;

import it.unimi.dsi.fastutil.longs.LongList;
import me.jellysquid.mods.lithium.common.cached_blockpos_iteration.IterateOutwardsCache;
import me.jellysquid.mods.lithium.common.cached_blockpos_iteration.LongList2BlockPosMutableIterable;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.jellysquid.mods.lithium.common.cached_blockpos_iteration.IterateOutwardsCache.POS_ZERO;

/**
 * @author 2No2Name, original implemenation by SuperCoder7979 and Gegy1000
 */
@Mixin(BlockPos.class)
public class BlockPosMixin {
    private static final IterateOutwardsCache ITERATE_OUTWARDS_CACHE = new IterateOutwardsCache(50);
    private static final LongList HOGLIN_PIGLIN_CACHE = ITERATE_OUTWARDS_CACHE.getOrCompute(8, 4, 8);

    @Inject(method = "iterateOutwards", at = @At("HEAD"), cancellable = true)
    private static void iterateOutwards(BlockPos center, int xRange, int yRange, int zRange, CallbackInfoReturnable<Iterable<BlockPos>> cir) {
        if (center == POS_ZERO) {
            //use vanilla code when we call iterateOutwards to fill the cache
            return;
        }
        //use our cache when it is called from other places
        //shortcut the most commonly used 8,4,8 range to skip map lookup
        final LongList positions = xRange == 8 && yRange == 4 && zRange == 8 ? HOGLIN_PIGLIN_CACHE : ITERATE_OUTWARDS_CACHE.getOrCompute(xRange, yRange, zRange);
        cir.setReturnValue(new LongList2BlockPosMutableIterable(center, positions));
    }


}
