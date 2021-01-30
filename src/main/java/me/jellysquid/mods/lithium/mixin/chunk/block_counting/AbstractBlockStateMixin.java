package me.jellysquid.mods.lithium.mixin.chunk.block_counting;

import me.jellysquid.mods.lithium.common.block.BlockStateFlagHolder;
import me.jellysquid.mods.lithium.common.block.IndexedBlockStatePredicate;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBlock.AbstractBlockState.class)
public class AbstractBlockStateMixin implements BlockStateFlagHolder {
    private int flags;

    @Inject(method = "initShapeCache", at = @At("RETURN"))
    private void init(CallbackInfo ci) {
        this.initFlags();
    }

    private void initFlags() {
        IndexedBlockStatePredicate.FULLY_INITIALIZED.set(true);

        int flags = 0;

        for (int i = 0; i < IndexedBlockStatePredicate.ALL_FLAGS.length; i++) {
            if (IndexedBlockStatePredicate.ALL_FLAGS[i].test((BlockState) (Object) this)) {
                flags |= 1 << i;
            }
        }

        this.flags = flags;
    }

    @Override
    public int getAllFlags() {
        return this.flags;
    }
}
