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

/**
 * This patch safely avoids excessive overhead in some hot methods by caching some constant values in the BlockState
 * itself, excluding dynamic dispatch and the pointer dereferences.
 */
@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockStateMixin {
    @Shadow
    protected abstract BlockState asBlockState();

    @Shadow
    public abstract Block getBlock();

    /**
     * The fluid state is constant for any given block state, so it can be safely cached. This notably improves performance
     * when scanning for fluid blocks.
     */
    private FluidState fluidStateCache = null;

    /**
     * Indicates whether the current block state can be ticked. Since this value is always the same for any given block state
     * and random block ticking is a frequent process during chunk ticking, in theory this is a very good change.
     */
    private boolean isTickable;

    /**
     * We can't use the ctor as a BlockState will be constructed *before* a Block has fully initialized.
     */
    @Inject(method = "initShapeCache", at = @At("HEAD"))
    private void init(CallbackInfo ci) {
        this.fluidStateCache = this.getBlock().getFluidState(this.asBlockState());
        this.isTickable = this.getBlock().hasRandomTicks(this.asBlockState());
    }

    /**
     * @reason Use cached property
     * @author JellySquid
     */
    @Overwrite
    public FluidState getFluidState() {
        if (this.fluidStateCache == null) {
            this.fluidStateCache = this.getBlock().getFluidState(this.asBlockState());
        }

        return this.fluidStateCache;
    }

    /**
     * @reason Use cached property
     * @author Maity
     */
    @Overwrite
    public boolean hasRandomTicks() {
        return this.isTickable;
    }
}
