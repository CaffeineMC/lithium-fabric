package me.jellysquid.mods.lithium.common.world.scheduler;

import it.unimi.dsi.fastutil.Arrays;

public class UpdateList<T> {
    @SuppressWarnings("unchecked")
    private ScheduledTickMap.TickEntry<T>[] arr = new ScheduledTickMap.TickEntry[ScheduledTickMap.INITIAL_UPDATE_LIST_CAPACITY];

    private int size;

    public final long key;

    public UpdateList(long key) {
        this.key = key;
    }

    public void add(ScheduledTickMap.TickEntry<T> k) {
        this.grow(this.size + 1);

        this.arr[this.size++] = k;
    }

    @SuppressWarnings("unchecked")
    private void grow(int capacity) {
        if (capacity <= this.arr.length) {
            return;
        }

        capacity = (int) Math.max(Math.min((long) this.arr.length + (this.arr.length >> 1), Arrays.MAX_ARRAY_SIZE), capacity);

        final ScheduledTickMap.TickEntry<T>[] t = new ScheduledTickMap.TickEntry[capacity];
        System.arraycopy(this.arr, 0, t, 0, this.size);
        this.arr = t;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public ScheduledTickMap.TickEntry<T> get(int i) {
        return this.arr[i];
    }

    public int size() {
        return this.size;
    }

    public void set(int j, ScheduledTickMap.TickEntry<T> k) {
        this.arr[j] = k;
    }
}
