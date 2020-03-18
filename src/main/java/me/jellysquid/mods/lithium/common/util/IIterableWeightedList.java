package me.jellysquid.mods.lithium.common.util;

import net.minecraft.util.collection.WeightedList;

import java.util.Iterator;

public interface IIterableWeightedList<U> extends Iterable<U> {
    Iterator<U> iterator();

    @SuppressWarnings("unchecked")
    static <T> Iterable<? extends T> cast(WeightedList<T> list) {
        return ((IIterableWeightedList<T>) list);
    }

    class ListIterator<U> implements Iterator<U> {
        private final Iterator<WeightedList<U>.Entry<? extends U>> inner;

        public ListIterator(Iterator<WeightedList<U>.Entry<? extends U>> inner) {
            this.inner = inner;
        }

        @Override
        public boolean hasNext() {
            return this.inner.hasNext();
        }

        @Override
        public U next() {
            return this.inner.next().getElement();
        }
    }
}
