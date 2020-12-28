package me.jellysquid.mods.lithium.mixin.gen.cached_generator_settings;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.util.Lazy;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;

@Mixin(NoiseChunkGenerator.class)
public class NoiseChunkGeneratorMixin {
    @Shadow
    @Final
    protected Supplier<ChunkGeneratorSettings> settings;
    private int cachedSeaLevel = Integer.MIN_VALUE;

    /**
     * Use cached sea level instead of retrieving from the registry every time.
     * This method is called for every block in the chunk so this will save a lot of registry lookups.
     *
     * @author SuperCoder79
     */
    @Overwrite
    public int getSeaLevel() {
        if (this.cachedSeaLevel == Integer.MIN_VALUE) {
            this.cachedSeaLevel = this.settings.get().getSeaLevel();
        }

        return this.cachedSeaLevel;
    }
}
