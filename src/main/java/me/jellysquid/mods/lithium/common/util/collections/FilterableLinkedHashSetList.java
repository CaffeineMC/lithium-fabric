package me.jellysquid.mods.lithium.common.util.collections;

import it.unimi.dsi.fastutil.longs.Long2ReferenceAVLTreeMap;
import it.unimi.dsi.fastutil.objects.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FilterableLinkedHashSetList<T> implements List<T> {
    private final Reference2LongLinkedOpenHashMap<T> delegate;
    private final Long2ReferenceAVLTreeMap<T> filtered;
    private long index;

    public FilterableLinkedHashSetList(List<T> list) {
        this.index = -1;
        this.delegate = new Reference2LongLinkedOpenHashMap<>();
        this.delegate.defaultReturnValue(-1);
        this.filtered = new Long2ReferenceAVLTreeMap<>();
        for (T t : list) {
            long l = this.nextIndex();
            this.delegate.put(t, l);
            this.filtered.put(l, t);
        }
    }
    
    private long nextIndex() {
        long l = ++this.index;
        if (l < 0) {
            this.reinitialize();
            //this.index cannot go anywhere near overflow inside reinitialize, because it is long, while the elements are addressed with integers.
            l = ++this.index;
        }
        return l;
    }

    private void reinitialize() {
        Reference2BooleanArrayMap<T> tmp = new Reference2BooleanArrayMap<>();
        for (Reference2LongMap.Entry<T> entry : this.delegate.reference2LongEntrySet()) {
            tmp.put(entry.getKey(), this.filtered.containsKey(entry.getLongValue()));
        }

        this.index = -1;
        this.filtered.clear();
        this.delegate.clear();
        for (ObjectIterator<Reference2BooleanMap.Entry<T>> iterator = tmp.reference2BooleanEntrySet().fastIterator(); iterator.hasNext(); ) {
            Reference2BooleanMap.Entry<T> entry = iterator.next();
            long l = ++this.index;
            T key = entry.getKey();
            this.delegate.put(key, l);
            if (entry.getBooleanValue()) {
                this.filtered.put(l, key);
            }
        }
    }

    public void setEntryVisible(T t, boolean value) {
        long index = this.delegate.getLong(t);
        if (index != -1) {
            if (value) {
                this.filtered.put(index, t);
            } else {
                this.filtered.remove(index);
            }
        }
    }

    public Iterator<T> filteredIterator() {
        return this.filtered.values().iterator();
    }

    @Override
    public int size() {
        return this.delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return this.delegate.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        //noinspection SuspiciousMethodCalls
        return this.delegate.containsKey(o);
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return this.delegate.keySet().iterator();
    }

    @Override
    public Object[] toArray() {
        return this.delegate.keySet().toArray();
    }

    @NotNull
    @Override
    public <T1> T1[] toArray(@NotNull T1[] t1s) {
        //noinspection SuspiciousToArrayCall
        return this.delegate.keySet().toArray(t1s);
    }

    @Override
    public boolean add(T t) {
        long l = this.nextIndex();
        this.delegate.put(t, l);
        this.filtered.put(l, t);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        long l = this.delegate.removeLong(o);
        if (l != -1) {
            this.filtered.remove(l);
        }
        return l != 0;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return this.delegate.keySet().containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        boolean b = false;
        for (T t : c) {
            long l = this.nextIndex();
            this.delegate.put(t, l);
            this.filtered.put(l, t);
            b = true;
        }
        return b;
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        boolean b = false;
        for (Object t : c) {
            long l = this.delegate.removeLong(t);
            if (l != -1) {
                this.filtered.remove(l);
            }
            b = true;
        }
        return b;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        this.delegate.clear();
        this.filtered.clear();
    }

    @Override
    public T get(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T set(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull ListIterator<T> listIterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull ListIterator<T> listIterator(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull List<T> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }
}
