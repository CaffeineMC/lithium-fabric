package me.jellysquid.mods.lithium.common.shapes.pairs;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.util.shape.PairList;

/**
 * Optimized variant of {@link net.minecraft.util.shape.SimplePairList}. This implementation works directly against
 * flat arrays and tries to organize code in a manner that hits the JIT's happy path. In my testing, this is about
 * ~50% faster than the vanilla implementation.
 */
public final class LithiumDoublePairList implements PairList {
    private final double[] merged;
    private final int[] indicesFirst;
    private final int[] indicesSecond;

    private final DoubleArrayList pairs;

    public LithiumDoublePairList(DoubleList aPoints, DoubleList bPoints, boolean flag1, boolean flag2) {
        final double[] aRaw;

        if (aPoints instanceof DoubleArrayList) {
            aRaw = ((DoubleArrayList) aPoints).elements();
        } else {
            aRaw = new double[aPoints.size()];

            for (int i = 0; i < aRaw.length; i++) {
                aRaw[i] = aPoints.getDouble(i);
            }
        }

        final double[] bRaw;

        if (bPoints instanceof DoubleArrayList) {
            bRaw = ((DoubleArrayList) bPoints).elements();
        } else {
            bRaw = new double[bPoints.size()];

            for (int i = 0; i < bRaw.length; i++) {
                bRaw[i] = bPoints.getDouble(i);
            }
        }

        final int size = aRaw.length + bRaw.length;

        this.merged = new double[size];
        this.indicesFirst = new int[size];
        this.indicesSecond = new int[size];

        this.pairs = DoubleArrayList.wrap(this.merged);

        this.merge(aRaw, bRaw, aRaw.length, bRaw.length, flag1, flag2);
    }

    private void merge(double[] aPoints, double[] bPoints, int aSize, int bSize, boolean flag1, boolean flag2) {
        int aIdx = 0;
        int bIdx = 0;

        double prev = 0.0D;

        int a1 = 0, a2 = 0;

        while (true) {
            final boolean aWithinBounds = aIdx < aSize;
            final boolean bWithinBounds = bIdx < bSize;

            if (!aWithinBounds && !bWithinBounds) {
                break;
            }

            final boolean flip = aWithinBounds && (!bWithinBounds || aPoints[aIdx] < bPoints[bIdx] + 1.0E-7D);
            final double value = flip ? aPoints[aIdx++] : bPoints[bIdx++];

            if ((aIdx == 0 || !aWithinBounds) && !flip && !flag2) {
                continue;
            }

            if ((bIdx == 0 || !bWithinBounds) && flip && !flag1) {
                continue;
            }

            if (a2 == 0 || prev < value - 1.0E-7D) {
                this.indicesFirst[a1] = aIdx - 1;
                this.indicesSecond[a1] = bIdx - 1;
                this.merged[a2] = value;

                a1++;
                a2++;
                prev = value;
            } else if (a2 > 0) {
                this.indicesFirst[a1 - 1] = aIdx - 1;
                this.indicesSecond[a1 - 1] = bIdx - 1;
            }
        }

        if (a2 == 0) {
            this.merged[a2++] = Math.min(aPoints[aSize - 1], bPoints[bSize - 1]);
        }

        this.pairs.size(a2);
    }

    @Override
    public boolean forEachPair(PairList.Consumer predicate) {
        int l = this.pairs.size() - 1;

        for (int i = 0; i < l; i++) {
            if (!predicate.merge(this.indicesFirst[i], this.indicesSecond[i], i)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public DoubleList getPairs() {
        return this.pairs;
    }
}
