package me.jellysquid.mods.lithium.common.util.collections;

import com.google.common.collect.AbstractIterator;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.UnaryOperator;

public class IteratorLazyList<E> implements List<E> {
    private final Iterator<E> iterator;
    private final ArrayList<E> elements;

    public IteratorLazyList(Iterator<E> iterator) {
        this.iterator = iterator;
        this.elements = new ArrayList<>();
    }

    @Override
    public boolean isEmpty() {
        return this.advanceTo(0);
    }

    @Override
    public E get(int index) {
        if (index - this.elements.size() >= 0) {
            this.advanceTo(index);
        }
        return this.elements.get(index);
    }

    @Override
    public E set(int index, E element) {
        this.advanceTo(index);
        return this.elements.set(index, element);
    }

    @Override
    public void add(int index, E element) {
        this.advanceTo(index);
        this.elements.add(index, element);
    }

    @Override
    public E remove(int index) {
        this.advanceTo(index);
        return this.elements.remove(index);
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        return new AbstractIterator<>() {
            int index = -1;

            @Override
            protected E computeNext() {
                if (IteratorLazyList.this.advanceTo(++this.index)) {
                    return this.endOfData();
                }
                return IteratorLazyList.this.elements.get(this.index);
            }
        };
    }

    @Override
    public boolean contains(Object o) {
        this.advanceTo(Integer.MAX_VALUE);
        return this.elements.contains(o);
    }

    @NotNull
    @Override
    public Object @NotNull [] toArray() {
        this.advanceTo(Integer.MAX_VALUE);
        return this.elements.toArray();
    }

    @NotNull
    @Override
    public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
        this.advanceTo(Integer.MAX_VALUE);
        //noinspection SuspiciousToArrayCall
        return this.elements.toArray(a);
    }

    @Override
    public boolean add(E e) {
        this.advanceTo(Integer.MAX_VALUE);
        return this.elements.add(e);
    }

    @Override
    public boolean remove(Object o) {
        this.advanceTo(Integer.MAX_VALUE);
        return this.elements.remove(o);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        this.advanceTo(Integer.MAX_VALUE);
        return this.elements.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        this.advanceTo(Integer.MAX_VALUE);
        return this.elements.addAll(c);
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends E> c) {
        this.advanceTo(Integer.MAX_VALUE);
        return this.elements.addAll(index, c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        this.advanceTo(Integer.MAX_VALUE);
        return this.elements.removeAll(c);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        this.advanceTo(Integer.MAX_VALUE);
        return this.elements.retainAll(c);
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        this.advanceTo(Integer.MAX_VALUE);
        this.elements.replaceAll(operator);
    }

    @Override
    public void sort(Comparator<? super E> c) {
        this.advanceTo(Integer.MAX_VALUE);
        this.elements.sort(c);
    }

    @Override
    public void clear() {
        this.advanceTo(Integer.MAX_VALUE);
        this.elements.clear();
    }

    @Override
    public int indexOf(Object o) {
        this.advanceTo(Integer.MAX_VALUE);
        return this.elements.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        this.advanceTo(Integer.MAX_VALUE);
        return this.elements.lastIndexOf(o);
    }

    @NotNull
    @Override
    public ListIterator<E> listIterator() {
        this.advanceTo(Integer.MAX_VALUE);
        return this.elements.listIterator();
    }

    @NotNull
    @Override
    public ListIterator<E> listIterator(int index) {
        this.advanceTo(Integer.MAX_VALUE);
        return this.elements.listIterator(index);
    }

    @NotNull
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        this.advanceTo(Integer.MAX_VALUE);
        return this.elements.subList(fromIndex, toIndex);
    }

    @Override
    public Spliterator<E> spliterator() {
        this.advanceTo(Integer.MAX_VALUE);
        return this.elements.spliterator();
    }

    @Override
    public int size() {
        this.advanceTo(Integer.MAX_VALUE);
        return this.elements.size();
    }

    /**
     * Evaluates the iterator up to a certain list index.
     *
     * @param targetIndex minimum list index to evaluate
     * @return whether there were not enough elements in the iterator
     */
    private boolean advanceTo(int targetIndex) {
        Iterator<E> it = this.iterator;
        targetIndex -= this.elements.size();
        while (targetIndex >= 0 && it.hasNext()) {
            E next = it.next();
            this.elements.add(next);
            if (targetIndex != Integer.MAX_VALUE) {
                targetIndex--;
            }
        }
        return targetIndex >= 0;
    }
}
