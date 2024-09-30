package me.jellysquid.mods.lithium.common.ai;

import java.util.Iterator;
import net.minecraft.world.entity.ai.behavior.ShufflingList;

public interface WeightedListIterable<U> extends Iterable<U> {
    /**
     * {@inheritDoc}
     */
    Iterator<U> iterator();

    /**
     * Returns an {@link Iterable} over the elements in the {@param list}. This allows code to circumvent the usage
     * of streams, providing a speed-up in other areas of the game.
     */
    @SuppressWarnings("unchecked")
    static <T> Iterable<? extends T> cast(ShufflingList<T> list) {
        return ((WeightedListIterable<T>) list);
    }

    /**
     * A wrapper type for an iterator over the entries of a {@link ShufflingList} which de-references the contained
     * values for consumers.
     *
     * @param <U> The value type stored in each list entry
     */
    class ListIterator<U> implements Iterator<U> {
        private final Iterator<ShufflingList.WeightedEntry<? extends U>> inner;

        public ListIterator(Iterator<ShufflingList.WeightedEntry<? extends U>> inner) {
            this.inner = inner;
        }

        @Override
        public boolean hasNext() {
            return this.inner.hasNext();
        }

        @Override
        public U next() {
            return this.inner.next().getData();
        }
    }
}
