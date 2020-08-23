package me.jellysquid.mods.lithium.common.util.streams;

import java.util.Comparator;
import java.util.Spliterator;
import java.util.function.Consumer;

abstract class ConcatSpliterator<T, S extends Spliterator<T>>
            implements Spliterator<T> {
        protected final S aSpliterator;
        protected final S bSpliterator;
        boolean beforeSplit;
        final boolean unsized;

        public ConcatSpliterator(S aSpliterator, S bSpliterator) {
            this.aSpliterator = aSpliterator;
            this.bSpliterator = bSpliterator;
            beforeSplit = true;
            unsized = aSpliterator.estimateSize() + bSpliterator.estimateSize() < 0;
        }

        @Override
        public S trySplit() {
            @SuppressWarnings("unchecked")
            final S ret = beforeSplit ? aSpliterator : (S) bSpliterator.trySplit();
            beforeSplit = false;
            return ret;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> consumer) {
            boolean hasNext;
            if (beforeSplit) {
                hasNext = aSpliterator.tryAdvance(consumer);
                if (!hasNext) {
                    beforeSplit = false;
                    hasNext = bSpliterator.tryAdvance(consumer);
                }
            }
            else
                hasNext = bSpliterator.tryAdvance(consumer);
            return hasNext;
        }

        @Override
        public void forEachRemaining(Consumer<? super T> consumer) {
            if (beforeSplit)
                aSpliterator.forEachRemaining(consumer);
            bSpliterator.forEachRemaining(consumer);
        }

        @Override
        public long estimateSize() {
            if (beforeSplit) {
                final long size = aSpliterator.estimateSize() + bSpliterator.estimateSize();
                return (size >= 0) ? size : Long.MAX_VALUE;
            } else {
                return bSpliterator.estimateSize();
            }
        }

        @Override
        public int characteristics() {
            return beforeSplit
                    ? (aSpliterator.characteristics() & bSpliterator.characteristics()

                    & ~(Spliterator.DISTINCT | Spliterator.SORTED
                    | (unsized ? Spliterator.SIZED | Spliterator.SUBSIZED : 0)))

                    : bSpliterator.characteristics();
        }

        @Override
        public Comparator<? super T> getComparator() {
            if (beforeSplit)
                throw new IllegalStateException();
            return bSpliterator.getComparator();
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
                if (beforeSplit) {
                    hasNext = aSpliterator.tryAdvance(action);
                    if (!hasNext) {
                        beforeSplit = false;
                        hasNext = bSpliterator.tryAdvance(action);
                    }
                }
                else
                    hasNext = bSpliterator.tryAdvance(action);
                return hasNext;
            }

            @Override
            public void forEachRemaining(T_CONS action) {
                if (beforeSplit)
                    aSpliterator.forEachRemaining(action);
                bSpliterator.forEachRemaining(action);
            }
        }

    }