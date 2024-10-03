package net.caffeinemc.mods.lithium.mixin.util.data_storage;

import net.caffeinemc.mods.lithium.common.world.LithiumData;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(Level.class)
public class LevelMixin implements LithiumData {

    @Unique
    private Data storage;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initLithiumData(WritableLevelData properties, ResourceKey<?> registryRef, RegistryAccess registryManager, Holder<?> dimensionEntry, Supplier<?> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates, CallbackInfo ci) {
        this.storage = new Data((Level) (Object) this);
    }

    @Override
    public Data lithium$getData() {
        return this.storage;
    }
}
