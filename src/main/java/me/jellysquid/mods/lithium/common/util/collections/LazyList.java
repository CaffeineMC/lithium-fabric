package me.jellysquid.mods.lithium.common.util.collections;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class LazyList<T> extends AbstractList<T> {

    private final ArrayList<T> delegate;
    private Iterator<T> iterator;

    public LazyList(ArrayList<T> delegate, Iterator<T> iterator) {
        this.delegate = delegate;
        this.iterator = iterator;
    }

    private boolean produceToIndex(int n) {
        n -= this.delegate.size();
        if (n >= 0 && this.iterator != null) {
            while (this.iterator.hasNext()) {
                this.delegate.add(this.iterator.next());
                if (--n < 0) {
                    return true;
                }
            }
            this.iterator = null;
        }
        return n < 0;
    }

    @Override
    public T get(int index) {
        this.produceToIndex(index);
        return this.delegate.get(index);
    }

    @Override
    public int size() {
        this.produceToIndex(Integer.MAX_VALUE);
        return this.delegate.size();
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return new Iterator<T>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return LazyList.this.produceToIndex(this.index);
            }

            @Override
            public T next() {
                return LazyList.this.get(this.index++);
            }
        };
    }

    @Override
    public T set(int index, T element) {
        this.produceToIndex(index);
        return this.delegate.set(index, element);
    }

    @Override
    public void add(int index, T element) {
        this.produceToIndex(index - 1);
        this.delegate.add(index, element);
    }

    @Override
    public T remove(int index) {
        this.produceToIndex(index);
        return this.delegate.remove(index);
    }

    @Override
    public void clear() {
        this.delegate.clear();
        this.iterator = null;
    }

    @Override
    public boolean add(T t) {
        this.produceToIndex(Integer.MAX_VALUE);
        return this.delegate.add(t);
    }

    @Override
    public boolean isEmpty() {
        return !this.produceToIndex(0);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        if (c.isEmpty()) {
            return false;
        }
        this.produceToIndex(index - 1);
        return this.delegate.addAll(index, c);
    }
}
