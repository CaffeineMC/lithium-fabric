package me.jellysquid.mods.lithium.mixin.chunk.count_oversized_blocks;

import me.jellysquid.mods.lithium.common.block.BlockStateFlags;
import me.jellysquid.mods.lithium.common.block.Flag;
import me.jellysquid.mods.lithium.common.block.FlagHolder;
import net.minecraft.block.AbstractBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBlock.AbstractBlockState.class)
public class MixinAbstractBlockState implements FlagHolder {
    private int flags;

    @Inject(method = "initShapeCache", at = @At("RETURN"))
    private void init(CallbackInfo ci) {
        this.initFlags();
    }

    private void initFlags() {
        Flag.CachedFlag.FULLY_INITIALIZED.set(true);

        int flags = 0;

        for (int i = 0; i < BlockStateFlags.NUM_FLAGS; i++) {
            if (Flag.CachedFlag.ALL_FLAGS[i].test((AbstractBlock.AbstractBlockState) (Object) this)) {
                flags |= 1 << i;
            }
        }

        this.flags = flags;
    }

    @Override
    public boolean getFlag(Flag.CachedFlag flag) {
        return (flag.getMask() & this.flags) != 0;
    }

    @Override
    public int getAllFlags() {
        return this.flags;
    }
}
