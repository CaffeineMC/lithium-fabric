package me.jellysquid.mods.lithium.common.util.collections;

import java.util.AbstractList;
import java.util.Iterator;

public class FakeIteratorList<E> extends AbstractList<E> {
    private final Iterator<E> iterator;

    public FakeIteratorList(Iterator<E> iterator) {
        this.iterator = iterator;
    }

    @Override
    public Iterator<E> iterator() {
        return this.iterator;
    }

    @Override
    public E get(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }
}
