package me.jellysquid.mods.lithium.mixin.util.data_storage;

import me.jellysquid.mods.lithium.common.world.LithiumData;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(World.class)
public class WorldMixin implements LithiumData {

    @Unique
    private LithiumData.Data storage;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initLithiumData(MutableWorldProperties properties, RegistryKey<?> registryRef, DynamicRegistryManager registryManager, RegistryEntry<?> dimensionEntry, Supplier<?> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates, CallbackInfo ci) {
        this.storage = new LithiumData.Data((World) (Object) this);
    }

    @Override
    public LithiumData.Data lithium$getData() {
        return this.storage;
    }
}
