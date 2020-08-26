package me.jellysquid.mods.lithium.mixin.chunk.no_locking;

import me.jellysquid.mods.lithium.common.util.lock.NullReentrantLock;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.collection.IdList;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * The implementation of {@link PalettedContainer} performs a strange check to catch concurrent modification and throws
 * an exception if it occurs. In practice, this never occurs and seems to be a left-over that was added when off-thread
 * chunk generation was being developed and tested. However, a poorly behaved mod could violate the thread-safety
 * contract and cause issues which would not be caught with this patch.
 *
 * This locking (according to some individuals) can impact performance significantly, though my own testing shows it to
 * have only a small impact (about 5% of the world generation time) in Java 11 on an AMD Piledriver-based system running
 * Linux 5.4. As the locking code is platform (and even implementation) specific, it's hard to make an absolute statement
 * about it. My experience has been that the Java locks tend to perform worse on Windows, which is what most players use.
 *
 * @author JellySquid
 */
@Mixin(PalettedContainer.class)
public class PalettedContainerMixin<T> {
    @Shadow
    @Mutable
    @Final
    private ReentrantLock writeLock;

    /**
     * Replacing the lock with its "empty" variant, which does nothing (and it does not use the lock and unlock code).
     *
     * @author Maity
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void reinitializeLock(Palette<T> fallbackPalette, IdList<T> idList,
                                  Function<CompoundTag, T> deserializer, Function<T, CompoundTag> serializer,
                                  T defaultElement, CallbackInfo ci) {
        this.writeLock = new NullReentrantLock();
    }
}
