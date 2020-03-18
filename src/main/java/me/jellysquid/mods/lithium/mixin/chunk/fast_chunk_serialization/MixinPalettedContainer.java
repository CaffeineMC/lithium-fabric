package me.jellysquid.mods.lithium.mixin.chunk.fast_chunk_serialization;

import me.jellysquid.mods.lithium.common.world.chunk.CompactingPackedIntegerArray;
import me.jellysquid.mods.lithium.common.world.chunk.palette.LithiumHashPalette;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.collection.IdList;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.ArrayPalette;
import net.minecraft.world.chunk.BiMapPalette;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

/**
 * Makes a number of patches to {@link PalettedContainer} to improve the performance of chunk serialization. While I/O
 * operations in Minecraft 1.15+ are handled off-thread, NBT serialization is not and happens on the main server thread.
 */
@Mixin(PalettedContainer.class)
public abstract class MixinPalettedContainer<T> {
    @Shadow
    public abstract void lock();

    @Shadow
    public abstract void unlock();

    @Shadow
    protected abstract T get(int index);

    @Shadow
    @Final
    private T field_12935;

    @Shadow
    @Final
    private IdList<T> idList;

    @Shadow
    private int paletteSize;

    @Shadow
    @Final
    private Function<CompoundTag, T> elementDeserializer;

    @Shadow
    @Final
    private Function<T, CompoundTag> elementSerializer;

    @Shadow
    protected PackedIntegerArray data;

    @Shadow
    private Palette<T> palette;

    /**
     * This patch incorporates a number of changes to significantly reduce the time needed to serialize.
     * - Iterate over the packed integer array using a specialized consumer instead of a naive for-loop.
     * - Maintain a temporary list of int->int mappings between the working data array and compacted data array. If a
     * mapping doesn't exist yet, create it in the compacted palette. This allows us to avoid the many lookups through
     * the palette and saves considerable time.
     * - If the palette didn't change size after compaction, avoid the step of copying all the data into a new packed
     * integer array and simply use a memcpy to clone the working array (storing it alongside the current working palette.)
     *
     * @reason Optimize serialization
     * @author JellySquid
     */
    @Inject(method = "write", at = @At("HEAD"), cancellable = true)
    public void write(CompoundTag tag, String paletteKey, String dataKey, CallbackInfo ci) {
        // We're using a fallback map and it doesn't need compaction!
        if (this.paletteSize > 8) {
            return;
        }

        this.lock();

        LithiumHashPalette<T> compactedPalette = new LithiumHashPalette<>(this.idList, this.paletteSize, null, this.elementDeserializer, this.elementSerializer);
        compactedPalette.getIndex(this.field_12935);

        short[] remapped = ((CompactingPackedIntegerArray) this.data)
                .compact(this.palette, compactedPalette, this.field_12935);

        int originalIntSize = this.data.getElementBits();
        int copyIntSize = Math.max(4, MathHelper.log2DeBruijn(compactedPalette.getSize()));

        // If the palette didn't change sizes, there's no reason to copy anything
        if (this.palette instanceof LithiumHashPalette && originalIntSize == copyIntSize) {
            long[] array = this.data.getStorage();
            long[] copy = new long[array.length];

            System.arraycopy(array, 0, copy, 0, array.length);

            ListTag paletteTag = new ListTag();
            ((LithiumHashPalette<T>) this.palette).toTag(paletteTag);

            tag.put(paletteKey, paletteTag);
            tag.putLongArray(dataKey, copy);
        } else {
            PackedIntegerArray copy = new PackedIntegerArray(copyIntSize, 4096);

            for (int i = 0; i < remapped.length; ++i) {
                copy.set(i, remapped[i]);
            }

            ListTag paletteTag = new ListTag();
            compactedPalette.toTag(paletteTag);

            tag.put(paletteKey, paletteTag);
            tag.putLongArray(dataKey, copy.getStorage());
        }

        this.unlock();

        ci.cancel();
    }

    /**
     * If we know the palette will contain a fixed number of elements, we can make a significant optimization by counting
     * blocks with a simple array instead of a integer map. Since palettes make no guarantee that they are bounded,
     * we have to try and determine for each implementation type how many elements there are.
     *
     * @author JellySquid
     */
    @Inject(method = "count", at = @At("HEAD"), cancellable = true)
    public void count(PalettedContainer.CountConsumer<T> consumer, CallbackInfo ci) {
        int size = getPaletteSize(this.palette);

        // We don't know how many items are in the palette, so this optimization cannot be done
        if (size < 0) {
            return;
        }

        int[] counts = new int[size];

        this.data.forEach(i -> counts[i]++);

        for (int i = 0; i < counts.length; i++) {
            consumer.accept(this.palette.getByIndex(i), counts[i]);
        }

        ci.cancel();
    }

    /**
     * Try to determine the number of elements in a palette, otherwise return -1 to indicate that it is unknown.
     */
    private static int getPaletteSize(Palette<?> palette) {
        if (palette instanceof BiMapPalette<?>) {
            return ((BiMapPalette<?>) palette).getIndexBits();
        } else if (palette instanceof LithiumHashPalette<?>) {
            return ((LithiumHashPalette<?>) palette).getSize();
        } else if (palette instanceof ArrayPalette<?>) {
            return ((ArrayPalette<?>) palette).getSize();
        }

        return -1;
    }
}
