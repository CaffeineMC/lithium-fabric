package me.jellysquid.mods.lithium.api.inventory;

public interface LithiumTransferConditionInventory {

    /**
     * Implement this method to signal that the inventory requires a stack size of 1 for item insertion tests.
     * Lithium's hopper optimization transfers a single item, but to avoid excessive copying of item stacks, it passes
     * the original stack to the inventory's insertion test. If the inventory requires a stack size of 1 for this test,
     * the stack should be copied. However, lithium cannot detect whether the copy is necessary and this method is meant
     * to signal this requirement. When the method is not implemented even though it is required, Lithium's hopper
     * optimizations may not transfer items correctly to this inventory.
     * <p>
     * The only vanilla inventory that requires this is the Chiseled Bookshelf. Mods with such special inventories
     * should implement this method in the inventories' class.
     * (It is not required to implement this interface, just the method is enough.)
     *
     * @return whether the inventory requires a stack size of 1 for item insertion tests
     */
    default boolean lithium$itemInsertionTestRequiresStackSize1() {
        return false;
    }
}
