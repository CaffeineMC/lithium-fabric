package me.jellysquid.mods.lithium.mixin.biome;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.VoronoiBiomeAccessType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(VoronoiBiomeAccessType.class)
public abstract class MixinVoronoiBiomeAccessType {
    /**
     * @reason Avoid memory allocations, use faster loop
     * @author JellySquid
     */
    @Overwrite
    public Biome getBiome(long seed, int x, int y, int z, BiomeAccess.Storage storage) {
        int x1 = x - 2;
        int y1 = y - 2;
        int z1 = z - 2;

        int x2 = x1 >> 2;
        int y2 = y1 >> 2;
        int z2 = z1 >> 2;

        double x3 = (double) (x1 & 3) / 4.0D;
        double y3 = (double) (y1 & 3) / 4.0D;
        double z3 = (double) (z1 & 3) / 4.0D;

        int retX = Integer.MIN_VALUE;
        int retY = Integer.MIN_VALUE;
        int retZ = Integer.MIN_VALUE;

        // This code would normally allocate an array to store each iteration's results, then scan back over it
        // to determine the closest one. We can avoid the unnecessary step and simply keep track of the nearest one.
        double min = Double.POSITIVE_INFINITY;

        for (int i = 0; i < 8; i++) {
            // Block sample positions
            int bX;
            int bY;
            int bZ;

            // Sample positions
            double sX;
            double sY;
            double sZ;

            if ((i & 0b100) == 0) {
                bX = x2;
                sX = x3;
            } else {
                bX = x2 + 1;
                sX = x3 - 1.0D;
            }

            if ((i & 0b010) == 0) {
                bY = y2;
                sY = y3;
            } else {
                bY = y2 + 1;
                sY = y3 - 1.0D;
            }

            if ((i & 0b001) == 0) {
                bZ = z2;
                sZ = z3;
            } else {
                bZ = z2 + 1;
                sZ = z3 - 1.0D;
            }

            double result = calcChance(seed, bX, bY, bZ, sX, sY, sZ);

            if (min > result) {
                min = result;

                retX = bX;
                retY = bY;
                retZ = bZ;
            }
        }

        return storage.getBiomeForNoiseGen(retX, retY, retZ);
    }

    /**
     * @reason Replace expensive modulo with simple bit shift
     * @author JellySquid
     */
    @Overwrite
    private static double distribute(long seed) {
        return ((((seed >> 24) & 1023L) / 1024.0D) - 0.5D) * 0.9D;
    }

    @Shadow
    private static double calcChance(long seed, int x, int y, int z, double xFraction, double yFraction, double zFraction) {
        throw new UnsupportedOperationException();
    }
}
