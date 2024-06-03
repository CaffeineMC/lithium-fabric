package me.jellysquid.mods.lithium.common.util.collections;

import com.google.common.collect.Iterators;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class LazyList<T> extends AbstractList<T> {

    private final ArrayList<T> delegate;
    private final ArrayList<Iterator<T>> iterators;

    public LazyList(ArrayList<T> delegate) {
        this.delegate = delegate;
        this.iterators = new ArrayList<>();
    }

    public void appendIterator(Iterator<T> iterator) {
        this.iterators.add(iterator);
    }

    private boolean produceToIndex(int n) {
        n -= this.delegate.size();
        while (n >= 0 && !this.iterators.isEmpty()) {
            Iterator<T> iterator = this.iterators.get(0);
            while (iterator.hasNext()) {
                this.delegate.add(iterator.next());
                if (--n < 0) {
                    return true;
                }
            }
            this.iterators.remove(0);
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
        this.iterators.clear();
    }

    @Override
    public boolean add(T t) {
        if (this.iterators.isEmpty()) {
            this.delegate.add(t);
        } else {
            this.appendIterator(Iterators.singletonIterator(t));
        }
        return true;
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
        this.appendIterator(new ArrayList<T>(c).iterator());
        return true;
    }
}
