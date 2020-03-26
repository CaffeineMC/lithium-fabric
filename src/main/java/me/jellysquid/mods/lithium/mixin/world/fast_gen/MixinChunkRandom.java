package me.jellysquid.mods.lithium.mixin.world.fast_gen;

import me.jellysquid.mods.lithium.common.world.gen.Rand;
import net.minecraft.world.gen.ChunkRandom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(ChunkRandom.class)
public abstract class MixinChunkRandom extends Random {

    @Unique
    private Rand rand;

    @Override
    public void setSeed(long seed) {
        if(this.rand == null) {
            this.rand = new Rand(seed, true);
        } else {
            this.rand.setSeed(seed, true);
        }
    }

    @Override
    public int next(int bits) {
        return this.rand.next(bits);
    }

    @Override
    public int nextInt() {
        return this.rand.nextInt();
    }

    @Override
    public int nextInt(int bound) {
        return this.rand.nextInt(bound);
    }

    @Override
    public boolean nextBoolean() {
        return this.rand.nextBoolean();
    }

    @Override
    public long nextLong() {
        return this.rand.nextLong();
    }

    @Override
    public float nextFloat() {
        return this.rand.nextFloat();
    }

    @Override
    public double nextDouble() {
        return this.rand.nextDouble();
    }

    /*
    * This function pains me as LCG skips can be run O(1) if you know the number of calls in advance.
    * Since we don't know it here though, we can compute it O(log(n)) instead of O(n) at least.
    * */
    @Inject(method = "consume", at = @At("HEAD"), cancellable = true)
    public void consume(int calls, CallbackInfo ci) {
        this.rand.advance(calls);
    }

}
