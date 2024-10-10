package net.caffeinemc.mods.lithium.mixin.chunk.serialization;

import net.caffeinemc.mods.lithium.common.world.chunk.CompactingPackedIntegerArray;
import net.caffeinemc.mods.lithium.common.world.chunk.LithiumHashPalette;
import net.minecraft.core.IdMap;
import net.minecraft.util.BitStorage;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.util.ZeroBitStorage;
import net.minecraft.world.level.chunk.Palette;
import net.minecraft.world.level.chunk.PaletteResize;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

/**
 * Makes a number of patches to {@link PalettedContainer} to speed up integer array compaction. While I/O operations
 * in Minecraft 1.15+ are handled off-thread, NBT serialization is not and happens on the main server thread.
 */
@Mixin(PalettedContainer.class)
public abstract class PalettedContainerMixin<T> {
    private static final ThreadLocal<short[]> CACHED_ARRAY_4096 = ThreadLocal.withInitial(() -> new short[4096]);
    private static final ThreadLocal<short[]> CACHED_ARRAY_64 = ThreadLocal.withInitial(() -> new short[64]);

    @Shadow
    public abstract void acquire();

    @Shadow
    protected abstract T get(int index);

    @Shadow
    private volatile PalettedContainer.Data<T> data;

    @Shadow
    public abstract void release();

    @Shadow
    @Final
    private PaletteResize<T> dummyPaletteResize;

    /**
     * This patch incorporates a number of changes to significantly reduce the time needed to serialize.
     * - If a palette only contains one entry, do not attempt to repack it
     * - The packed integer array is iterated over using a specialized consumer instead of a naive for-loop.
     * - A temporary fixed array is used to cache palette lookups and remaps while compacting a data array.
     * - If the palette didn't change after compaction, avoid the step of re-packing the integer array and instead do
     * a simple memory copy.
     *
     * @reason Optimize serialization
     * @author JellySquid
     */
    @Overwrite
    public PalettedContainerRO.PackedData<T> pack(IdMap<T> idList, PalettedContainer.Strategy provider) {
        this.acquire();

        // The palette that will be serialized
        LithiumHashPalette<T> hashPalette = null;
        Optional<LongStream> data = Optional.empty();
        List<T> elements = null;

        final Palette<T> palette = this.data.palette();
        final BitStorage storage = this.data.storage();
        if (storage instanceof ZeroBitStorage || palette.getSize() == 1) {
            // If the palette only contains one entry, don't attempt to repack it.
            elements = List.of(palette.valueFor(0));
        } else if (palette instanceof LithiumHashPalette<T> lithiumHashPalette) {
            hashPalette = lithiumHashPalette;
        }

        if (elements == null) {
            LithiumHashPalette<T> compactedPalette = new LithiumHashPalette<>(idList, storage.getBits(), this.dummyPaletteResize);
            short[] array = this.getOrCreate(provider.size());

            ((CompactingPackedIntegerArray) storage).lithium$compact(this.data.palette(), compactedPalette, array);

            // If the palette didn't change during compaction, do a simple copy of the data array
            if (hashPalette != null && hashPalette.getSize() == compactedPalette.getSize() && storage.getBits() == provider.calculateBitsForSerialization(idList, hashPalette.getSize())) { // paletteSize can de-sync from palette - see https://github.com/CaffeineMC/lithium-fabric/issues/279
                data = this.asOptional(storage.getRaw().clone());
                elements = hashPalette.getElements();
            } else {
                int bits = provider.calculateBitsForSerialization(idList, compactedPalette.getSize());
                if (bits != 0) {
                    // Re-pack the integer array as the palette has changed size
                    SimpleBitStorage copy = new SimpleBitStorage(bits, array.length);
                    for (int i = 0; i < array.length; ++i) {
                        copy.set(i, array[i]);
                    }

                    // We don't need to clone the data array as we are the sole owner of it
                    data = this.asOptional(copy.getRaw());
                }

                elements = compactedPalette.getElements();
            }
        }

        this.release();
        return new PalettedContainerRO.PackedData<>(elements, data);
    }

    private Optional<LongStream> asOptional(long[] data) {
        return Optional.of(Arrays.stream(data));
    }

    private short[] getOrCreate(int size) {
        return switch (size) {
            case 64 -> CACHED_ARRAY_64.get();
            case 4096 -> CACHED_ARRAY_4096.get();
            default -> new short[size];
        };
    }

    /**
     * If we know the palette will contain a fixed number of elements, we can make a significant optimization by counting
     * blocks with a simple array instead of a integer map. Since palettes make no guarantee that they are bounded,
     * we have to try and determine for each implementation type how many elements there are.
     *
     * @author JellySquid
     */
    @Inject(method = "count(Lnet/minecraft/world/level/chunk/PalettedContainer$CountConsumer;)V", at = @At("HEAD"), cancellable = true)
    public void count(PalettedContainer.CountConsumer<T> consumer, CallbackInfo ci) {
        int len = this.data.palette().getSize();

        // Do not allocate huge arrays if we're using a large palette
        if (len > 4096) {
            return;
        }

        short[] counts = new short[len];

        this.data.storage().getAll(i -> counts[i]++);

        for (int i = 0; i < counts.length; i++) {
            T obj = this.data.palette().valueFor(i);

            if (obj != null) {
                consumer.accept(obj, counts[i]);
            }
        }

        ci.cancel();
    }
}
