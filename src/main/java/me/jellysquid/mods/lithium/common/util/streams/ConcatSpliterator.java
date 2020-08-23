package me.jellysquid.mods.lithium.common.util.streams;

import java.util.Comparator;
import java.util.Spliterator;
import java.util.function.Consumer;

abstract class ConcatSpliterator<T, S extends Spliterator<T>>
        implements Spliterator<T> {
    protected final S aSpliterator;
    protected final S bSpliterator;
    boolean beforeSplit;
    private final boolean unsized;

    public ConcatSpliterator(S aSpliterator, S bSpliterator) {
        this.aSpliterator = aSpliterator;
        this.bSpliterator = bSpliterator;
        this.beforeSplit = true;
        this.unsized = this.aSpliterator.estimateSize() + this.bSpliterator.estimateSize() < 0;
    }

    @Override
    public S trySplit() {
        @SuppressWarnings("unchecked") final S ret = this.beforeSplit ? this.aSpliterator : (S) this.bSpliterator.trySplit();
        this.beforeSplit = false;
        return ret;
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> consumer) {
        boolean hasNext;
        if (this.beforeSplit) {
            hasNext = this.aSpliterator.tryAdvance(consumer);
            if (!hasNext) {
                this.beforeSplit = false;
                hasNext = this.bSpliterator.tryAdvance(consumer);
            }
        } else {
            hasNext = this.bSpliterator.tryAdvance(consumer);
        }
        return hasNext;
    }

    @Override
    public void forEachRemaining(Consumer<? super T> consumer) {
        if (this.beforeSplit) {
            this.aSpliterator.forEachRemaining(consumer);
        }
        this.bSpliterator.forEachRemaining(consumer);
    }

    @Override
    public long estimateSize() {
        if (this.beforeSplit) {
            final long size = this.aSpliterator.estimateSize() + this.bSpliterator.estimateSize();
            return (size >= 0) ? size : Long.MAX_VALUE;
        } else {
            return this.bSpliterator.estimateSize();
        }
    }

    @Override
    public int characteristics() {
        return this.beforeSplit
                ? (this.aSpliterator.characteristics() & this.bSpliterator.characteristics()

                & ~(Spliterator.DISTINCT | Spliterator.SORTED
                | (this.unsized ? Spliterator.SIZED | Spliterator.SUBSIZED : 0)))

                : this.bSpliterator.characteristics();
    }

    @Override
    public Comparator<? super T> getComparator() {
        if (this.beforeSplit) {
            throw new IllegalStateException();
        }
        return this.bSpliterator.getComparator();
    }

    static class OfRef<T> extends ConcatSpliterator<T, Spliterator<T>> {
        OfRef(Spliterator<T> aSpliterator, Spliterator<T> bSpliterator) {
            super(aSpliterator, bSpliterator);
        }
    }

    private abstract static class OfPrimitive<T, T_CONS, S extends Spliterator.OfPrimitive<T, T_CONS, S>>
            extends ConcatSpliterator<T, S>
            implements Spliterator.OfPrimitive<T, T_CONS, S> {
        private OfPrimitive(S aSpliterator, S bSpliterator) {
            super(aSpliterator, bSpliterator);
        }

        @Override
        public boolean tryAdvance(T_CONS action) {
            boolean hasNext;
            if (this.beforeSplit) {
                hasNext = this.aSpliterator.tryAdvance(action);
                if (!hasNext) {
                    beforeSplit = false;
                    hasNext = this.bSpliterator.tryAdvance(action);
                }
            } else {
                hasNext = this.bSpliterator.tryAdvance(action);
            }
            return hasNext;
        }

        @Override
        public void forEachRemaining(T_CONS action) {
            if (this.beforeSplit) {
                this.aSpliterator.forEachRemaining(action);
            }
            this.bSpliterator.forEachRemaining(action);
        }
    }

}