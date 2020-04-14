package me.jellysquid.mods.lithium.common.util.tag;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.tag.Tag;

import java.util.List;
import java.util.Set;

/**
 * An implementation of {@link Tag} which uses a hash table to scan elements.
 */
public class HashTag<T> implements Tag<T> {
    private final ObjectArrayList<T> list;
    private final ObjectOpenHashSet<T> set;

    public HashTag(Set<T> set) {
        this.list = new ObjectArrayList<>(set);
        this.set = new ObjectOpenHashSet<>(set);
    }

    @Override
    public boolean contains(T entry) {
        return this.set.contains(entry);
    }

    @Override
    public List<T> values() {
        return this.list;
    }
}
