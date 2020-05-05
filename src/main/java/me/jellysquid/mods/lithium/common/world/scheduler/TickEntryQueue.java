package me.jellysquid.mods.lithium.common.world.scheduler;

import it.unimi.dsi.fastutil.HashCommon;

/**
 * Minimal implementation of a array de-queue which supports skipping over elements during iteration by pushing them to
 * the head of the array. This provides a sizable performance boost over a {@link java.util.LinkedList} due to the
 * improved cache locality and doesn't require allocations or extra conditional logic to re-build the array from the
 * skipped elements.
 */
public class TickEntryQueue<T> {
    private static final int INITIAL_CAPACITY = 16;

    private TickEntry<T>[] arr;

    private int size;

    @SuppressWarnings("unchecked")
    public TickEntryQueue(int capacity) {
        this.arr = (TickEntry<T>[]) new TickEntry[capacity];
        this.size = 0;
    }

    public TickEntryQueue() {
        this(INITIAL_CAPACITY);
    }

    public void push(TickEntry<T> tick) {
        if (this.size >= this.arr.length) {
            this.arr = copyArray(this.arr, HashCommon.nextPowerOfTwo(this.arr.length + 1));
        }

        this.arr[this.size++] = tick;
    }

    public int size() {
        return this.size;
    }

    public void resize(int size) {
        // Only compact the array if it is completely empty or is less than 50% filled
        if (size == 0 || size < this.arr.length / 2) {
            this.arr = copyArray(this.arr, size);
        } else {
            // Fill the unused array elements with nulls to release our references to the elements in it
            for (int i = size; i < this.arr.length; i++) {
                this.arr[i] = null;
            }
        }

        this.size = size;
    }

    public TickEntry<T> getTickAtIndex(int index) {
        return this.arr[index];
    }

    public void setTickAtIndex(int index, TickEntry<T> tick) {
        this.arr[index] = tick;
    }

    @SuppressWarnings("unchecked")
    private static <T> TickEntry<T>[] copyArray(TickEntry<T>[] src, int size) {
        final TickEntry<T>[] copy = new TickEntry[size];

        if (size != 0) {
            System.arraycopy(src, 0, copy, 0, Math.min(src.length, size));
        }

        return copy;
    }

    public boolean isEmpty() {
        return this.size <= 0;
    }
}
