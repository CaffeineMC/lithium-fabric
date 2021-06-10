package me.jellysquid.mods.lithium.common.util.collections;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.*;
import java.util.function.Consumer;

public class MaskedList<E> extends AbstractList<E> {
    private final ObjectArrayList<E> allElements;
    private final BitSet visibleMask;

    public MaskedList(ObjectArrayList<E> allElements) {
        this.allElements = allElements;
        this.visibleMask = new BitSet();
    }

    public void setVisible(E element, final boolean visible) {
        int i = -1;
        if (visible) {
            do {
                i = this.visibleMask.nextClearBit(i + 1);
            } while (element != this.allElements.get(i));
            this.visibleMask.set(i);
        } else {
            do {
                i = this.visibleMask.nextSetBit(i + 1);
            } while (element != this.allElements.get(i));
            this.visibleMask.clear(i);
        }
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            int nextIndex = 0;

            @Override
            public boolean hasNext() {
                return MaskedList.this.visibleMask.nextSetBit(this.nextIndex) != -1;
            }

            @Override
            public E next() {
                int index = MaskedList.this.visibleMask.nextSetBit(this.nextIndex);
                this.nextIndex = index + 1;
                return MaskedList.this.allElements.get(index);
            }
        };
    }

    @Override
    public Spliterator<E> spliterator() {
        return new Spliterators.AbstractSpliterator<E>(Long.MAX_VALUE, Spliterator.ORDERED | Spliterator.NONNULL) {
            int nextIndex = 0;

            @Override
            public boolean tryAdvance(Consumer<? super E> action) {
                int index = MaskedList.this.visibleMask.nextSetBit(this.nextIndex);
                if (index == -1) {
                    return false;
                }
                this.nextIndex = index + 1;
                action.accept(MaskedList.this.allElements.get(index));
                return true;
            }
        };
    }

    @Override
    public E get(int index) {
        if (index < 0 || index >= this.size()) {
            throw new IndexOutOfBoundsException(index);
        }

        int i = 0;
        while (index >= 0) {
            index--;
            i = this.visibleMask.nextSetBit(i + 1);
        }
        return this.allElements.get(i);
    }

    @Override
    public int size() {
        return this.visibleMask.cardinality();
    }
}
