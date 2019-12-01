package me.jellysquid.mods.lithium.common.cache;

abstract class AbstractCachedAccess {
    static class CachedEntry<T> {
        T obj;
        long pos;

        CachedEntry() {
            this.reset();
        }

        void reset() {
            this.obj = null;
            this.pos = Integer.MIN_VALUE;
        }
    }
}
