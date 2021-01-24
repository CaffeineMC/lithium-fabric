package me.jellysquid.mods.lithium.mixin.gen.perlin_noise;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(PerlinNoiseSampler.class)
public class PerlinNoiseSamplerMixin {
    private static final int GRADIENT_STRIDE = 4;
    private static final int GRADIENT_STRIDE_SH = 2;

    @Shadow
    @Final
    private byte[] permutations;

    @Shadow
    @Final
    public double originX;

    @Shadow
    @Final
    public double originY;

    @Shadow
    @Final
    public double originZ;

    private final byte[] gradientTable = new byte[256 * GRADIENT_STRIDE];

    @Inject(method = "<init>", at = @At("RETURN"))
    private void reinit(Random random, CallbackInfo ci) {
        for (int i = 0; i < 256; i++) {
            int hash = this.permutations[i & 255] & 15;

            for (int j = 0; j < 3; j++) {
                this.gradientTable[(i * GRADIENT_STRIDE) + j] = (byte) SimplexNoiseSampler.GRADIENTS[hash][j];
            }
        }
    }

    /**
     * @reason Remove frequent type conversions
     * @author JellySquid
     */
    @Overwrite
    public double sample(double x, double y, double z, double d, double e) {
        final double ox = x + this.originX;
        final double oy = y + this.originY;
        final double oz = z + this.originZ;

        final double fox = Math.floor(ox);
        final double foy = Math.floor(oy);
        final double foz = Math.floor(oz);

        double oox = ox - fox;
        double ooy = oy - foy;
        double ooz = oz - foz;

        final double fx = MathHelper.perlinFade(oox);
        final double fy = MathHelper.perlinFade(ooy);
        final double fz = MathHelper.perlinFade(ooz);

        if (d != 0.0D) {
            ooy = ooy - (Math.floor(Math.min(e, ooy) / d) * d);
        }

        return this.sample((int) fox, (int) foy, (int) foz, oox, ooy, ooz, fx, fy, fz);
    }

    /**
     * This implementation makes a number of changes to reduce the CPU overhead of the function.
     * - A flattened gradients array is used to avoid pointer indirection
     * - Interpolation logic is optimized to remove unnecessary duplication of work between nested lerp calls
     * - Methods are inlined and optimized to reduce the instruction count as much as possible
     * - Math operations are re-organized into vertical array multiplications to help aid the JVM in vectorization
     *
     * @reason Optimize noise sampling
     * @author JellySquid
     */
    @Overwrite
    public double sample(int sectionX, int sectionY, int sectionZ, double localX1, double localY1, double localZ1, double fadeLocalX, double fadeLocalY, double fadeLocalZ) {
        final byte[] perm = this.permutations;

        final int i = (perm[sectionX & 255] & 255) + sectionY;
        final int l = (perm[(sectionX + 1) & 255] & 255) + sectionY;

        final int j = (perm[255 & i] & 255) + sectionZ;
        final int m = (perm[l & 255] & 255) + sectionZ;

        final int k = (perm[(i + 1) & 255] & 255) + sectionZ;
        final int n = (perm[(l + 1) & 255] & 255) + sectionZ;

        final double localX2 = localX1 - 1.0D;
        final double localY2 = localY1 - 1.0D;
        final double localZ2 = localZ1 - 1.0D;

        final int d00 = (j & 255) << GRADIENT_STRIDE_SH;
        final int d01 = (m & 255) << GRADIENT_STRIDE_SH;
        final int d02 = (k & 255) << GRADIENT_STRIDE_SH;
        final int d03 = (n & 255) << GRADIENT_STRIDE_SH;

        final int d10 = ((j + 1) & 255) << GRADIENT_STRIDE_SH;
        final int d11 = ((m + 1) & 255) << GRADIENT_STRIDE_SH;
        final int d12 = ((k + 1) & 255) << GRADIENT_STRIDE_SH;
        final int d13 = ((n + 1) & 255) << GRADIENT_STRIDE_SH;

        final byte[] grad = this.gradientTable;

        final double g00x = grad[d00] * localX1;
        final double g00y = grad[d00 + 1] * localY1;
        final double g00z = grad[d00 + 2] * localZ1;

        final double g01x = grad[d01] * localX2;
        final double g01y = grad[d01 + 1] * localY1;
        final double g01z = grad[d01 + 2] * localZ1;

        final double g02x = grad[d02] * localX1;
        final double g02y = grad[d02 + 1] * localY2;
        final double g02z = grad[d02 + 2] * localZ1;

        final double g03x = grad[d03] * localX2;
        final double g03y = grad[d03 + 1] * localY2;
        final double g03z = grad[d03 + 2] * localZ1;

        final double g10x = grad[d10] * localX1;
        final double g10y = grad[d10 + 1] * localY1;
        final double g10z = grad[d10 + 2] * localZ2;

        final double g11x = grad[d11] * localX2;
        final double g11y = grad[d11 + 1] * localY1;
        final double g11z = grad[d11 + 2] * localZ2;

        final double g12x = grad[d12] * localX1;
        final double g12y = grad[d12 + 1] * localY2;
        final double g12z = grad[d12 + 2] * localZ2;

        final double g13x = grad[d13] * localX2;
        final double g13y = grad[d13 + 1] * localY2;
        final double g13z = grad[d13 + 2] * localZ2;

        final double g00 = g00x + g00y + g00z;
        final double g01 = g01x + g01y + g01z;
        final double g02 = g02x + g02y + g02z;
        final double g03 = g03x + g03y + g03z;
        final double g10 = g10x + g10y + g10z;
        final double g11 = g11x + g11y + g11z;
        final double g12 = g12x + g12y + g12z;
        final double g13 = g13x + g13y + g13z;

        final double ba1 = g01 - g00;
        final double ba2 = g11 - g10;
        final double dc1 = g03 - g02;
        final double dc2 = g13 - g12;

        final double dba1 = fadeLocalX * ba1;
        final double dba2 = fadeLocalX * ba2;
        final double ddc1 = fadeLocalX * dc1;
        final double ddc2 = fadeLocalX * dc2;

        final double dd0 = g00 + dba1;
        final double dd1 = g10 + dba2;
        final double dd2 = g02 + ddc1;
        final double dd3 = g12 + ddc2;

        final double aa0 = dd2 - dd0;
        final double aa1 = dd3 - dd1;

        final double y20 = fadeLocalY * aa0;
        final double y31 = fadeLocalY * aa1;

        final double aa2 = dd0 + y20;
        final double aa3 = dd1 + y31;

        return dd0 + y20 + (fadeLocalZ * (aa3 - aa2));
    }
}
