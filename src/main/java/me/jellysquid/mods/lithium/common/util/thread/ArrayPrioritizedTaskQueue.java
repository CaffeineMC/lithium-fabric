package me.jellysquid.mods.lithium.common.util.thread;

import com.google.common.collect.Queues;
import net.minecraft.util.thread.TaskQueue;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A much, much faster implementation of TaskQueue.Prioritized which avoids excessive overhead when checking for
 * messages in the queue by avoiding usages of the Stream API. The improvement from this change can be most noticeably
 * seen when enqueueing light updates which occur during block updates.
 */
public class ArrayPrioritizedTaskQueue implements TaskQueue<TaskQueue.PrioritizedTask, Runnable> {
    // A simple array type is used to avoid needing to allocate an iterator to work on the queue
    private final Queue<Runnable>[] queues;

    // Instead of iterating over every queue to determine if work is present, we simply store a counter
    private final AtomicInteger size;

    @SuppressWarnings("unchecked")
    public ArrayPrioritizedTaskQueue(int count) {
        this.queues = new Queue[count];

        for (int i = 0; i < count; i++) {
            this.queues[i] = Queues.newConcurrentLinkedQueue();
        }

        this.size = new AtomicInteger(0);
    }

    @Override
    public Runnable poll() {
        for (Queue<Runnable> queue : this.queues) {
            Runnable task = queue.poll();

            if (task != null) {
                this.size.decrementAndGet();

                return task;
            }
        }

        return null;
    }

    @Override
    public boolean add(TaskQueue.PrioritizedTask task) {
        this.size.incrementAndGet();

        Queue<Runnable> queue = this.queues[task.getPriority()];
        queue.add(task);

        return true;
    }

    @Override
    public boolean isEmpty() {
        return this.size.get() <= 0;
    }
}
