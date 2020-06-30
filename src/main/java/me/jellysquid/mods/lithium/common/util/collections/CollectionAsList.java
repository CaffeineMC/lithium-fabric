package me.jellysquid.mods.lithium.common.util.collections;

import java.util.*;
import java.util.stream.Stream;

/**
 * A hacky way to use any collection as {@link List}.
 *
 * @author Maity
 */
public class CollectionAsList<E> implements List<E> {
    private final Collection<E> backed;

    public CollectionAsList(Collection<E> backed) {
        this.backed = backed;
    }

    @Override
    public int size() {
        return this.backed.size();
    }

    @Override
    public boolean isEmpty() {
        return this.backed.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.backed.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return this.backed.iterator();
    }

    @Override
    public Object[] toArray() {
        return this.backed.toArray();
    }

    @Override
    public String toString() {
        return this.backed.toString();
    }

    @Override
    public int hashCode() {
        return this.backed.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return this.backed.equals(o);
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return this.backed.toArray(a);
    }

    @Override
    public boolean add(E e) {
        return this.backed.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return this.backed.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.backed.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return this.backed.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return this.backed.retainAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return this.backed.retainAll(c);
    }

    @Override
    public void clear() {
        this.backed.clear();
    }

    @Override
    public Stream<E> stream() {
        return this.backed.stream();
    }

    @Override
    public Stream<E> parallelStream() {
        return this.backed.parallelStream();
    }

    @Override
    public Spliterator<E> spliterator() {
        return this.backed.spliterator();
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException("Lithium does not support this");
    }

    @Override
    public E get(int index) {
        throw new UnsupportedOperationException("Lithium does not support this");
    }

    @Override
    public E set(int index, E element) {
        throw new UnsupportedOperationException("Lithium does not support this");
    }

    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException("Lithium does not support this");
    }

    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException("Lithium does not support this");
    }

    @Override
    public int indexOf(Object o) {
        throw new UnsupportedOperationException("Lithium does not support this");
    }

    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException("Lithium does not support this");
    }

    @Override
    public ListIterator<E> listIterator() {
        throw new UnsupportedOperationException("Lithium does not support this");
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        throw new UnsupportedOperationException("Lithium does not support this");
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("Lithium does not support this");
    }
}
