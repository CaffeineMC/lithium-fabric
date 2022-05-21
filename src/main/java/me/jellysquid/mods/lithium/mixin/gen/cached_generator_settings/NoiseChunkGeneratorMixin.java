package me.jellysquid.mods.lithium.mixin.gen.cached_generator_settings;

import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NoiseChunkGenerator.class)
public class NoiseChunkGeneratorMixin {

    @Shadow
    @Final
    protected RegistryEntry<ChunkGeneratorSettings> settings;
    private int cachedSeaLevel;

    /**
     * Use cached sea level instead of retrieving from the registry every time.
     * This method is called for every block in the chunk so this will save a lot of registry lookups.
     *
     * @author SuperCoder79
     * @reason avoid registry lookup
     */
    @Overwrite
    public int getSeaLevel() {
        return this.cachedSeaLevel;
    }

    /**
     * Initialize the cache early in the ctor to avoid potential future problems with uninitialized usages
     */
    @SuppressWarnings("rawtypes")
    @Inject(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/registry/RegistryEntry;value()Ljava/lang/Object;",
                    shift = At.Shift.BEFORE
            )
    )
    private void hookConstructor(Registry structureSetRegistry, Registry noiseRegistry, BiomeSource populationSource, RegistryEntry registryEntry, CallbackInfo ci) {
        this.cachedSeaLevel = this.settings.value().seaLevel();
    }
}
