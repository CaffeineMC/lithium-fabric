package me.jellysquid.mods.lithium.common.block;

import net.minecraft.block.BlockState;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public abstract class IndexedBlockStatePredicate implements Predicate<BlockState> {
    public static final AtomicBoolean FULLY_INITIALIZED;
    public static final IndexedBlockStatePredicate[] ALL_FLAGS;
    private static final AtomicInteger NEXT_INDEX;

    static {
        FULLY_INITIALIZED = new AtomicBoolean(false);
        NEXT_INDEX = new AtomicInteger(0);
        ALL_FLAGS = new IndexedBlockStatePredicate[BlockStateFlags.NUM_FLAGS];
        if (!BlockStateFlags.ENABLED) { //classload the BlockStateFlags class which initializes the content of ALL_FLAGS
            System.out.println("Lithium Cached BlockState Flags are disabled!");
        }
    }

    private final int index;
    private final int mask;

    public IndexedBlockStatePredicate() {
        if (FULLY_INITIALIZED.get()) {
            throw new IllegalStateException("Lithium Cached BlockState Flags: Cannot register more flags after assuming to be fully initialized.");
        }
        this.index = NEXT_INDEX.getAndIncrement();
        if (this.index > 31 || this.index >= BlockStateFlags.NUM_FLAGS) {
            throw new IndexOutOfBoundsException();
        }
        this.mask = 1 << this.index;

        //initialization is run on one thread with synchronization afterwards, so escaping this here is fine
        ALL_FLAGS[this.index] = this;

        if (FULLY_INITIALIZED.get()) {
            throw new IllegalStateException("Lithium Cached BlockState Flags: Cannot register more flags after assuming to be fully initialized.");
        }
    }

    public int getIndex() {
        return this.index;
    }

    public int getMask() {
        return this.mask;
    }
}
