package me.jellysquid.mods.lithium.mixin.avoid_allocations;

import net.minecraft.util.collection.SortedArraySet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;
import java.util.function.Predicate;

@Mixin(SortedArraySet.class)
public abstract class MixinSortedArraySet<T> implements Collection<T> {
    @Shadow
    private int size;

    @Shadow
    private T[] elements;

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        T[] arr = this.elements;

        int writeLim = this.size;
        int writeIdx = 0;

        for (int readIdx = 0; readIdx < writeLim; readIdx++) {
            T obj = arr[readIdx];

            if (filter.test(obj)) {
                continue;
            }

            if (writeIdx != readIdx) {
                arr[writeIdx] = obj;
            }

            writeIdx++;
        }

        this.size = writeIdx;

        return writeLim != writeIdx;
    }
}
