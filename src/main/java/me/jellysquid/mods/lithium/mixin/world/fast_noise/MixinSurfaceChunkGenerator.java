package me.jellysquid.mods.lithium.mixin.world.fast_noise;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.OctavePerlinNoiseSampler;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import net.minecraft.world.gen.chunk.SurfaceChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SurfaceChunkGenerator.class)
public class MixinSurfaceChunkGenerator {

    @Shadow @Final private OctavePerlinNoiseSampler lowerInterpolatedNoise;

    @Shadow @Final private OctavePerlinNoiseSampler upperInterpolatedNoise;

    @Shadow @Final private OctavePerlinNoiseSampler interpolationNoise;

    /**
     * @reason Smarter use of perlin noise that avoids unneeded sampling.
     * @author SuperCoder79
     */
    @Overwrite
    private double sampleNoise(int x, int y, int z, double horizontalScale, double verticalScale, double horizontalStretch, double verticalStretch) {
        // To generate it's terrain, Minecraft uses two different perlin noises.
        // It interpolates these two noises to create the final sample at a position.
        // However, the interpolation noise is not all that good and spends most of it's time at > 1 or < 0, rendering
        // one of the noises completely unnecessary in the process.
        // By taking advantage of that, we can reduce the sampling needed per block through the interpolation noise.

        // This controls both the frequency and amplitude of the noise.
        double frequency = 1.0;
        double interpolationValue = 0.0;

        // Calculate interpolation data to decide what noise to sample.
        for (int octave = 0; octave < 8; octave++) {
            interpolationValue += sampleOctave(this.interpolationNoise.getOctave(octave), x, y, z, horizontalStretch, verticalStretch, frequency);
            frequency /= 2.0;
        }

        double clampedInterpolation = (interpolationValue / 10.0D + 1.0D) / 2.0D;

        if (clampedInterpolation >= 1) {
            // Sample only upper noise, as the lower noise will be interpolated out.
            frequency = 1.0;
            double noise = 0.0;
            for (int octave = 0; octave < 16; octave++) {
                noise += sampleOctave(this.upperInterpolatedNoise.getOctave(octave), x, y, z, horizontalScale, verticalScale, frequency);

                frequency /= 2.0;
            }

            return noise / 512.0D;
        } else if (clampedInterpolation <= 0) {
            // Sample only lower noise, as the upper noise will be interpolated out.
            frequency = 1.0;
            double noise = 0.0;
            for (int octave = 0; octave < 16; octave++) {
                noise += sampleOctave(this.lowerInterpolatedNoise.getOctave(octave), x, y, z, horizontalScale, verticalScale, frequency);

                frequency /= 2.0;
            }

            return noise / 512.0D;
        } else {
            // [VanillaCopy] SurfaceChunkGenerator#sampleNoise
            // Sample both and interpolate, as in vanilla.

            frequency = 1.0;
            double lowerNoise = 0.0;
            double upperNoise = 0.0;

            for (int octave = 0; octave < 16; octave++) {
                upperNoise += sampleOctave(this.upperInterpolatedNoise.getOctave(octave), x, y, z, horizontalScale, verticalScale, frequency);
                lowerNoise += sampleOctave(this.lowerInterpolatedNoise.getOctave(octave), x, y, z, horizontalScale, verticalScale, frequency);

                frequency /= 2.0;
            }

            // Vanilla behavior, return interpolated noise
            return MathHelper.lerp(clampedInterpolation, lowerNoise / 512.0, upperNoise / 512);
        }
    }

    private static double sampleOctave(PerlinNoiseSampler sampler, int x, int y, int z, double horizontalScale, double verticalScale, double frequency) {
        double scaledVerticalScale = verticalScale * frequency;
        return sampler.sample(
                OctavePerlinNoiseSampler.maintainPrecision(x * horizontalScale * frequency),
                OctavePerlinNoiseSampler.maintainPrecision(scaledVerticalScale * frequency),
                OctavePerlinNoiseSampler.maintainPrecision(z * horizontalScale * frequency), scaledVerticalScale, y * scaledVerticalScale) / frequency;
    }
}