package me.jellysquid.mods.lithium.common.block;

import net.minecraft.block.AbstractBlock;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static me.jellysquid.mods.lithium.common.block.BlockStateFlags.NUM_FLAGS;

public abstract class Flag<B> {
    public abstract boolean test(B operand);

    public abstract static class CachedFlag extends Flag<AbstractBlock.AbstractBlockState> {
        public static final AtomicBoolean FULLY_INITIALIZED;
        private static final AtomicInteger NEXT_INDEX;
        public static final CachedFlag[] ALL_FLAGS;

        static {
            FULLY_INITIALIZED = new AtomicBoolean(false);
            NEXT_INDEX = new AtomicInteger(0);
            ALL_FLAGS = new CachedFlag[NUM_FLAGS];
            if (!BlockStateFlags.ENABLED) { //classload the BlockStateFlags class which initializes the content of ALL_FLAGS
                System.out.println("Lithium Cached BlockState Flags are disabled!");
            }
        }

        private final int index;
        private final int mask;

        public CachedFlag() {
            if (FULLY_INITIALIZED.get()) {
                throw new IllegalStateException("Lithium Cached BlockState Flags: Cannot register more flags after assuming to be fully initialized.");
            }
            this.index = NEXT_INDEX.getAndIncrement();
            if (this.index > 31 || this.index >= NUM_FLAGS) {
                throw new IndexOutOfBoundsException();
            }
            this.mask = 1 << index;

            //initialization is run on one thread with synchronization afterwards, so escaping this here is fine
            ALL_FLAGS[this.index] = this;

            if (FULLY_INITIALIZED.get()) {
                throw new IllegalStateException("Lithium Cached BlockState Flags: Cannot register more flags after assuming to be fully initialized.");
            }
        }

        public int getIndex() {
            return index;
        }

        public int getMask() {
            return mask;
        }
    }
}
