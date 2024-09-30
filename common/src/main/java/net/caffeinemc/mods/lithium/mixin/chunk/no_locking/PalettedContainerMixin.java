package net.caffeinemc.mods.lithium.mixin.chunk.no_locking;

import net.minecraft.util.ThreadingDetector;
import net.minecraft.world.level.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The implementation of {@link PalettedContainer} performs a strange check to catch concurrent modification and throws
 * an exception if it occurs. In practice, this never occurs and seems to be a left-over that was added when off-thread
 * chunk generation was being developed and tested. However, a poorly behaved mod could violate the thread-safety
 * contract and cause issues which would not be caught with this patch.
 * <p>
 * This locking (according to some individuals) can impact performance significantly, though my own testing shows it to
 * have only a small impact (about 5% of the world generation time) in Java 11 on an AMD Piledriver-based system running
 * Linux 5.4. As the locking code is platform (and even implementation) specific, it's hard to make an absolute statement
 * about it. My experience has been that the Java locks tend to perform worse on Windows, which is what most players use.
 * <p>
 * This is a rather, well, dumb patch, to remove the locking mechanic. It would likely be wiser to implement a faster
 * check against some counter after modification has occurred to see if something was updated beneath our feet, though
 * that would not be guaranteed to catch the issue.
 */
@Mixin(PalettedContainer.class)
public class PalettedContainerMixin {
    @Shadow
    @Final
    @Mutable
    private ThreadingDetector threadingDetector;

    @Inject(
            method = {
                    "<init>(Lnet/minecraft/core/IdMap;Ljava/lang/Object;Lnet/minecraft/world/level/chunk/PalettedContainer$Strategy;)V",
                    "<init>(Lnet/minecraft/core/IdMap;Lnet/minecraft/world/level/chunk/PalettedContainer$Strategy;Lnet/minecraft/world/level/chunk/PalettedContainer$Configuration;Lnet/minecraft/util/BitStorage;Ljava/util/List;)V",
                    "<init>(Lnet/minecraft/core/IdMap;Lnet/minecraft/world/level/chunk/PalettedContainer$Strategy;Lnet/minecraft/world/level/chunk/PalettedContainer$Data;)V",
            },
            at = @At("TAIL")
    )
    public void removeLockHelper(CallbackInfo ci) {
        this.threadingDetector = null;
    }

    /**
     * @reason Do not check the container's lock
     * @author JellySquid
     */
    @Overwrite
    public void acquire() {

    }

    /**
     * @reason Do not check the container's lock
     * @author JellySquid
     */
    @Overwrite
    public void release() {

    }
}
