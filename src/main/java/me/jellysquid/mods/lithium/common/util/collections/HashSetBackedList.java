package me.jellysquid.mods.lithium.common.util.collections;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * A hacky way to use {@link HashSet} as {@link List} in Sponge's Mixin Environment.
 *
 * @param <E> the type of elements maintained by this backed collection
 *
 * @author Maity
 */
public class HashSetBackedList<E> implements List<E> {
    private final Collection<E> backedCollection;

    public HashSetBackedList() {
        this.backedCollection = new HashSet<>();
    }

    @Override
    public int size() {
        return this.backedCollection.size();
    }

    @Override
    public boolean isEmpty() {
        return this.backedCollection.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.backedCollection.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return this.backedCollection.iterator();
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        this.backedCollection.forEach(action);
    }

    @Override
    public Object[] toArray() {
        return this.backedCollection.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return this.backedCollection.toArray(a);
    }

    @Override
    public boolean add(E e) {
        return this.backedCollection.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return this.backedCollection.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.backedCollection.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return this.backedCollection.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return this.backedCollection.removeAll(c);
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        return this.backedCollection.removeIf(filter);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return this.backedCollection.retainAll(c);
    }

    @Override
    public void clear() {
        this.backedCollection.clear();
    }

    @Override
    public Spliterator<E> spliterator() {
        return this.backedCollection.spliterator();
    }

    @Override
    public Stream<E> stream() {
        return this.backedCollection.stream();
    }

    @Override
    public Stream<E> parallelStream() {
        return this.backedCollection.parallelStream();
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        return false;
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator) {

    }

    @Override
    public void sort(Comparator<? super E> c) {

    }

    @Override
    public E get(int index) {
        return null;
    }

    @Override
    public E set(int index, E element) {
        return null;
    }

    @Override
    public void add(int index, E element) {

    }

    @Override
    public E remove(int index) {
        return null;
    }

    @Override
    public int indexOf(Object o) {
        return 0;
    }

    @Override
    public int lastIndexOf(Object o) {
        return 0;
    }

    @Override
    public ListIterator<E> listIterator() {
        return null;
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return null;
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return null;
    }

    @Override
    public final int hashCode() {
        return this.backedCollection.hashCode();
    }

    @Override
    public final boolean equals(Object o) {
        return this.backedCollection.equals(o);
    }

    @Override
    public final String toString() {
        return this.backedCollection.toString();
    }
}
