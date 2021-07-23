package me.jellysquid.mods.lithium.api.inventory;

import net.minecraft.inventory.Inventory;

import java.util.ArrayList;
import java.util.function.ToIntBiFunction;

/**
 * Usage of this API class requires enabling mixin.block.hopper.compat !
 * <p>
 * Provides the ability for mods to specify that hoppers can currently not access an inventory. The mod needs to
 * implement the behavior for vanilla code. Lithium will respect the given conditions in lithium's code paths.
 * The condition result is not cached and will be calculated every time a hopper ticks.
 */
public interface LithiumHopperCompat {
    ArrayList<ToIntBiFunction<Inventory, Inventory>> ITEM_TRANSFER_CONDITIONS = new ArrayList<>();

    /**
     * Usage of this API class requires enabling mixin.block.hopper.compat !
     *
     *
     * Method that allows mods to specify whether they deny item transfers from one inventory to another.
     * If any condition denies item transfers, there won't be a transfer attempt.
     *
     * @param allowTransferFromTo Function that maps the source inventory and the target inventory to an integer.
     *                            Iff the integer result is 0, the transfer will be denied.
     *                            ToBooleanBiFunction does not exist, therefore using ToIntBiFunction.
     */
    default void addTransferCondition(ToIntBiFunction<Inventory, Inventory> allowTransferFromTo) {
        ITEM_TRANSFER_CONDITIONS.add(allowTransferFromTo);
    }
}
