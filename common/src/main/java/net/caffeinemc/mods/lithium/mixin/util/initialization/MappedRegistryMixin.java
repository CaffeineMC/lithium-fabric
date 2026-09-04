package net.caffeinemc.mods.lithium.mixin.util.initialization;

import net.caffeinemc.mods.lithium.common.initialization.BlockInfoInitializer;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * We must invalidate our cached information which is based on block tags when the block tags change.
 * This hooks into the tag reload code, which is used when block tags change.
 */
@Mixin(MappedRegistry.class)
public abstract class MappedRegistryMixin<T> implements WritableRegistry<T> {

    @Inject(
            method = "prepareTagReload",
            at = @At("RETURN")
    )
    private void invalidateBlockInfo(TagLoader.LoadResult<T> tags, CallbackInfoReturnable<Registry.PendingTags<T>> cir) {
        //Only if this registry is about block tags. Maybe we will need to act on other registries in the future.
        //Currently, our cached information is only based on the block tags registry.

        //noinspection deprecation
        if (Blocks.AIR.builtInRegistryHolder().key().registry() == this.key().identifier()) {
            BlockInfoInitializer.resetBlockInfo();
        }
    }
}
