package me.jellysquid.mods.lithium.mixin.block.flatten_states;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
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
     * dereferences, dynamic dispatch, and bounds check of calling into BaseFluid to retrieve a fluid.
     * <p>
     * The fluid state is constant for any given block state, therefore making it safe to cache it. However, the block
     * implementation may not be initialized fully before this block state is constructed.
     * <p>
     * If this value is null, it is assumed that the value has not been cached and that we should fall back to calling
     * Block#getFluidState(BlockState).
     */
    private FluidState fluidState = null;

    /**
     * A block status cache that determines whether the current block state is ticking. This is slightly
     * speeds up the chunk ticking and random block ticking.
     */
    private boolean isTicking;

    /**
     * We can't use the ctor as a BlockState will be constructed *before* a Block has fully initialized.
     */
    @Inject(method = "initShapeCache", at = @At("HEAD"))
    private void initCaches(CallbackInfo ci) {
        this.isTicking = this.getBlock().hasRandomTicks(this.asBlockState());

        // In Minecraft code, @Deprecated usually does not actually mean @Deprecated.
        // In Block, deprecated methods mean "override, not call". This is because there is a corresponding
        // method in BlockState that we must call.
        this.fluidState = this.getBlock().getFluidState(this.asBlockState());
    }

    /**
     * @reason Use cached property
     * @author JellySquid, Maity
     */
    @Overwrite
    public boolean hasRandomTicks() {
        return this.isTicking;
    }

    /**
     * @reason Use cached property
     * @author JellySquid
     */
    @Overwrite
    public FluidState getFluidState() {
        if (this.fluidState == null) {
            this.fluidState = this.getBlock().getFluidState(this.asBlockState());
        }

        return this.fluidState;
    }
}
