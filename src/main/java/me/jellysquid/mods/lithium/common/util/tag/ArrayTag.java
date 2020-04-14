package me.jellysquid.mods.lithium.common.util.tag;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.tag.Tag;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * An implementation of {@link Tag} which uses a fixed array table to scan elements.
 */
public class ArrayTag<T> implements Tag<T> {
    private final Object[] arr;
    private final List<T> list;

    public ArrayTag(Set<T> set) {
        this.arr = set.toArray();
        this.list = Collections.unmodifiableList(new ObjectArrayList<>(set));
    }

    @Override
    public boolean contains(T entry) {
        for (Object obj : this.arr) {
            if (obj == entry) {
                return true;
            }
        }

        return false;
    }

    @Override
    public List<T> values() {
        return this.list;
    }
}
