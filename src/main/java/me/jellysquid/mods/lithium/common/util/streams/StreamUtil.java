package me.jellysquid.mods.lithium.common.util.streams;

import java.util.Objects;
import java.util.Spliterator;
import java.util.stream.BaseStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class StreamUtil {
    public static <T> Stream<T> concat(Stream<? extends T> a, Stream<? extends T> b) {
        @SuppressWarnings("unchecked")
        final Spliterator<T> split = new ConcatSpliterator.OfRef<>(
                (Spliterator<T>) a.spliterator(), (Spliterator<T>) b.spliterator());
        final Stream<T> stream = StreamSupport.stream(split, a.isParallel() || b.isParallel());
        return stream.onClose(composedClose(a, b));
    }

    public static Runnable composedClose(BaseStream<?, ?> a, BaseStream<?, ?> b) {
        return () -> {
            try {
                a.close();
            }
            catch (Throwable e1) {
                try {
                    b.close();
                }
                catch (Throwable e2) {
                    try {
                        e1.addSuppressed(e2);
                    } catch (Throwable ignore) {}
                }
                throw e1;
            }
            b.close();
        };
    }
}
