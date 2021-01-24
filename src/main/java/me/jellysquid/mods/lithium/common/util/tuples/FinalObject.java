package me.jellysquid.mods.lithium.common.util.tuples;

/**
 * The purpose of this class is safe publication of the wrapped value. (JLS 17.5)
 */
public class FinalObject<T> {
    private final T value;

    public FinalObject(T value) {
        this.value = value;
    }

    public FinalObject<T> of(T value) {
        return new FinalObject<>(value);
    }

    public T getValue() {
        return value;
    }
}
