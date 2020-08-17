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
    private final Collection<E> backedC;

    public HashSetBackedList() {
        this.backedC = new HashSet<>();
    }

    @Override
    public int size() {
        return this.backedC.size();
    }

    @Override
    public boolean isEmpty() {
        return this.backedC.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.backedC.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return this.backedC.iterator();
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        this.backedC.forEach(action);
    }

    @Override
    public Object[] toArray() {
        return this.backedC.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return this.backedC.toArray(a);
    }

    @Override
    public boolean add(E e) {
        return this.backedC.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return this.backedC.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.backedC.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return this.backedC.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return this.backedC.removeAll(c);
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        return this.backedC.removeIf(filter);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return this.backedC.retainAll(c);
    }

    @Override
    public void clear() {
        this.backedC.clear();
    }

    @Override
    public Spliterator<E> spliterator() {
        return this.backedC.spliterator();
    }

    @Override
    public Stream<E> stream() {
        return this.backedC.stream();
    }

    @Override
    public Stream<E> parallelStream() {
        return this.backedC.parallelStream();
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
        return this.backedC.hashCode();
    }

    @Override
    public final boolean equals(Object o) {
        return this.backedC.equals(o);
    }

    @Override
    public final String toString() {
        return this.backedC.toString();
    }
}
