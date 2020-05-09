package me.jellysquid.mods.lithium.common.util.collections;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;

import java.util.*;

/**
 * Wraps a {@link List} with a hash table which provides O(1) lookups for {@link Collection#contains(Object)}.
 */
public class HashedList<T> implements List<T> {
    private final List<T> list;
    private final Set<T> set;

    private HashedList(List<T> list, Set<T> set) {
        this.list = list;
        this.set = set;
    }

    @Override
    public int size() {
        return this.list.size();
    }

    @Override
    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.set.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return this.list.iterator();
    }

    @Override
    public Object[] toArray() {
        return this.list.toArray();
    }

    @SuppressWarnings("SuspiciousToArrayCall")
    @Override
    public <T1> T1[] toArray(T1[] a) {
        return this.list.toArray(a);
    }

    @Override
    public boolean add(T t) {
        this.set.add(t);

        return this.list.add(t);
    }

    @Override
    public boolean remove(Object o) {
        this.set.remove(o);

        return this.list.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.set.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        this.set.addAll(c);

        return this.list.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        this.set.addAll(c);

        return this.list.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        this.set.removeAll(c);

        return this.list.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        this.set.retainAll(c);

        return this.list.retainAll(c);
    }

    @Override
    public void clear() {
        this.set.clear();
        this.list.clear();
    }

    @Override
    public T get(int index) {
        return this.list.get(index);
    }

    @Override
    public T set(int index, T element) {
        T prev = this.list.set(index, element);

        if (prev != null) {
            this.set.remove(prev);
        }

        this.set.add(element);

        return prev;
    }

    @Override
    public void add(int index, T element) {
        this.set.add(element);

        this.list.add(index, element);
    }

    @Override
    public T remove(int index) {
        T prev = this.list.remove(index);

        if (prev != null) {
            this.set.remove(prev);
        }

        return prev;
    }

    @Override
    public int indexOf(Object o) {
        return this.list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return this.list.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return this.list.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return this.list.listIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return this.list.subList(fromIndex, toIndex);
    }

    public static <T> HashedList<T> wrapper(List<T> list) {
        return new HashedList<>(list, new ObjectArraySet<>(list));
    }
}
