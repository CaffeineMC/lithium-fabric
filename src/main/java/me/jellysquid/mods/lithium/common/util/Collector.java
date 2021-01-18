package me.jellysquid.mods.lithium.common.util;

public interface Collector<T> {
    /**
     * Collects the passed object and performs additional processing on it, returning a flag as to whether or not
     * collection should continue.
     *
     * @return True if collection should continue, otherwise false.
     */
    boolean collect(T obj);
}
