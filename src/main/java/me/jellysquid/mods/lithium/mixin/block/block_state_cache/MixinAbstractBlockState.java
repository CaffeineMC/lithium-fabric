package me.jellysquid.mods.lithium.mixin.block.block_state_cache;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.state.property.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class MixinAbstractBlockState {
    @Shadow
    protected abstract BlockState asBlockState();

    @Shadow
    public abstract Block getBlock();

    /**
     * We can avoid excessive overhead in looking up the fluid state of a block by caching those values in the
     * BlockState itself. This notably improves performance when scanning for fluid blocks by eliminating the pointer
     * dereferences, dynamic dispatch, and bounds check of calling into BaseFluid to retrieve a fluid. The fluid state
     * is constant for any given block state, therefore making it safe to cache it.
     */
    private FluidState fluidState;

    /**
     * We can't use the ctor as a BlockState will be constructed *before* a Block has fully initialized.
     */
    @Inject(method = "initShapeCache", at = @At("HEAD"))
    private void init(CallbackInfo ci) {
        this.fluidState = this.getBlock().getFluidState(this.asBlockState());
    }

    /**
     * @reason Use cached property
     * @author JellySquid
     */
    @Overwrite
    public FluidState getFluidState() {
        return this.fluidState == null ? this.getBlock().getFluidState(this.asBlockState()) : this.fluidState;
    }
}
