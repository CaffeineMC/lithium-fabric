package me.jellysquid.mods.lithium.mixin.world.biome_access;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.SeedMixer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Optimized getBiome call: Reduce the number of calls to the mess of
 * {@link net.minecraft.world.biome.source.SeedMixer#mixSeed(long, long)} which is pretty heavy on performance.
 *
 * We are able to do this by skipping around 370 of 512 possible calls to getBiome() by predicting the outcome
 * before doing the seed mixing. This seems to be around 25% - 75% faster depending on the use case.
 * We can predict much faster than the seed mixing.
 *
 * @author FX - PR0CESS
 */
@Mixin(BiomeAccess.class)
public class BiomeAccessMixin {

    @Shadow
    @Final
    private BiomeAccess.Storage storage;

    @Shadow
    @Final
    private long seed;

    @Shadow
    private static double method_38108(long l) {return 0;}

    /**
     * @author FX - PR0CESS
     * @reason I wanted to make it faster
     */
    @Overwrite
    public Biome getBiome(BlockPos pos) {
        int xMinus2 = pos.getX() - 2;
        int yMinus2 = pos.getY() - 2;
        int zMinus2 = pos.getZ() - 2;
        int x = xMinus2 >> 2; // BlockPos to BiomePos
        int y = yMinus2 >> 2;
        int z = zMinus2 >> 2;
        double quartX = (double)(xMinus2 & 3) / 4.0D; // quartLocal divided by 4
        double quartY = (double)(yMinus2 & 3) / 4.0D; // 0/4, 1/4, 2/4, 3/4
        double quartZ = (double)(zMinus2 & 3) / 4.0D; // [0, 0.25, 0.5, 0.75]
        int smallestX = 0;
        double smallestDist = Double.POSITIVE_INFINITY;
        for(int biomeX = 0; biomeX < 8; ++biomeX) {
            boolean everyOtherQuad = (biomeX & 4) == 0; // 1 1 1 1 0 0 0 0
            boolean everyOtherPair = (biomeX & 2) == 0; // 1 1 0 0 1 1 0 0
            boolean everyOther =     (biomeX & 1) == 0; // 1 0 1 0 1 0 1 0
            double quartXX = everyOtherQuad ? quartX : quartX - 1.0D; //[-1.0, -0.75, -0.5, -0.25, 0.0, 0.25, 0.5, 0.75]
            double quartYY = everyOtherPair ? quartY : quartY - 1.0D;
            double quartZZ = everyOther ? quartZ : quartZ - 1.0D;

            //This code block is new
            double maxQuartYY = 0.0D,maxQuartZZ = 0.0D;
            if (biomeX != 0) {
                maxQuartYY = MathHelper.square(Math.max(quartYY + 0.4500000001D, Math.abs(quartYY - 0.4500000001D)));
                maxQuartZZ = MathHelper.square(Math.max(quartZZ + 0.4500000001D, Math.abs(quartZZ - 0.4500000001D)));
                double maxQuartXX = MathHelper.square(Math.max(quartXX+0.4500000001D, Math.abs(quartXX-0.4500000001D)));
                if (smallestDist < maxQuartXX + maxQuartYY + maxQuartZZ) {
                    continue;
                }
            }

            int xx = everyOtherQuad ? x : x + 1;
            int yy = everyOtherPair ? y : y + 1;
            int zz = everyOther ? z : z + 1;

            //I transferred the code from method_38106 to here, so I could call continue halfway through
            long seed = SeedMixer.mixSeed(this.seed, xx);
            seed = SeedMixer.mixSeed(seed, yy);
            seed = SeedMixer.mixSeed(seed, zz);
            seed = SeedMixer.mixSeed(seed, xx);
            seed = SeedMixer.mixSeed(seed, yy);
            seed = SeedMixer.mixSeed(seed, zz);
            double offsetX = method_38108(seed);
            double sqrX = MathHelper.square(quartXX + offsetX);
            if (biomeX != 0 && smallestDist < sqrX + maxQuartYY + maxQuartZZ) {
                continue; // skip the rest of the loop
            }
            seed = SeedMixer.mixSeed(seed, this.seed);
            double offsetY = method_38108(seed);
            double sqrY = MathHelper.square(quartYY + offsetY);
            if (biomeX != 0 && smallestDist < sqrX + sqrY + maxQuartZZ) {
                continue; // skip the rest of the loop
            }
            seed = SeedMixer.mixSeed(seed, this.seed);
            double offsetZ = method_38108(seed);
            double biomeDist = sqrX + sqrY + MathHelper.square(quartZZ + offsetZ);

            if (smallestDist > biomeDist) {
                smallestX = biomeX;
                smallestDist = biomeDist;
            }
        }

        //Back to the orignal code
        int biomeX = (smallestX & 4) == 0 ? x : x + 1;
        int biomeY = (smallestX & 2) == 0 ? y : y + 1;
        int biomeZ = (smallestX & 1) == 0 ? z : z + 1;
        return this.storage.getBiomeForNoiseGen(biomeX, biomeY, biomeZ);
    }
}
