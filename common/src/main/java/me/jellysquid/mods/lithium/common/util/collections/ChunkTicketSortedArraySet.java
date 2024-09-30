package me.jellysquid.mods.lithium.common.util.collections;

import java.util.Comparator;
import net.minecraft.server.level.Ticket;
import net.minecraft.util.SortedArraySet;

public class ChunkTicketSortedArraySet<T> extends SortedArraySet<Ticket<?>> {

    private long minExpireTime = Long.MAX_VALUE;

    public ChunkTicketSortedArraySet(int initialCapacity) {
        super(initialCapacity, Comparator.naturalOrder());
    }

    public void addExpireTime(long time) {
        if (this.minExpireTime != Long.MAX_VALUE || this.isEmpty()) {
            this.minExpireTime = Math.min(this.minExpireTime, time);
        }
    }

    private void addExpireTimeInternal(long time) {
        this.minExpireTime = Math.min(this.minExpireTime, time);
    }

    public long getMinExpireTime() {
        if (this.minExpireTime == Long.MAX_VALUE) {
            this.recalculateExpireTime();
        }
        return minExpireTime;
    }

    @Override
    public boolean remove(Object object) {
        this.invalidateExpireTime();
        return super.remove(object);
    }
    
    public void invalidateExpireTime() {
        this.minExpireTime = Long.MAX_VALUE;
    }

    public void recalculateExpireTime() {
        this.minExpireTime = Long.MAX_VALUE;
        for (Ticket<?> c : this) {
            this.addExpireTimeInternal((c.createdTick + c.getType().timeout()));
        }
    }
}
