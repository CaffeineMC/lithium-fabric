package me.jellysquid.mods.lithium.common.util.math;

import java.util.Objects;

public class LCG {

    public final long multiplier;
    public final long addend;
    public final long modulo;

    private final boolean canMask;

    public LCG(long multiplier, long addend, long modulo) {
        this.multiplier = multiplier;
        this.addend = addend;
        this.modulo = modulo;

        this.canMask = (this.modulo & -this.modulo) == this.modulo;
    }

    public long nextSeed(long seed) {
        if(this.canMask) {
            return (seed * this.multiplier + this.addend) & (this.modulo - 1);
        }

        return (seed * this.multiplier + this.addend) % this.modulo;
    }

    public LCG combine(long steps) {
        long multiplier = 1;
        long addend = 0;

        long intermediateMultiplier = this.multiplier;
        long intermediateAddend = this.addend;

        for(long k = steps; k != 0; k >>>= 1) {
            if((k & 1) != 0) {
                multiplier *= intermediateMultiplier;
                addend = intermediateMultiplier * addend + intermediateAddend;
            }

            intermediateAddend = (intermediateMultiplier + 1) * intermediateAddend;
            intermediateMultiplier *= intermediateMultiplier;
        }

        if(this.canMask) {
            multiplier &= (this.modulo - 1);
            addend &= (this.modulo - 1);
        } else {
            multiplier %= this.modulo;
            addend %= this.modulo;
        }

        return new LCG(multiplier, addend, this.modulo);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this)return true;
        if(!(obj instanceof LCG))return false;
        LCG lcg = (LCG)obj;

        return this.multiplier == lcg.multiplier &&
                this.addend == lcg.addend &&
                this.modulo == lcg.modulo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.multiplier, this.addend, this.modulo);
    }

    @Override
    public String toString() {
        return "LCG{" + "multiplier=" + this.multiplier +
                ", addend=" + this.addend + ", modulo=" + this.modulo + '}';
    }

}
