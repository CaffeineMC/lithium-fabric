# Lithium Hopper Optimizations

## Assumptions made about other inventories

(likely incomplete)
Inventories that implement restrictions for hopper item insertion do not depend on the size of the inserted stack.
Hoppers always transfer a single item, inventories do not reject a stack if they are able to receive a single item from
the stack (the receiving inventory slot is not filled to its limit and the item type is correct). This allows lithium to
avoid creating a copy of the transferred item stack when the transfer is going to fail.

## Inventory Stack List

LithiumStackList replaces the inventory stack lists. It keeps an inventory content modification counter and caches the
signal strength until the inventory content is modified.

## Lithium Inventory

LithiumInventory is any inventory that can have its stack list replaced with LithiumStackList. Making any inventory a
LithiumInventory just requires implementing the getters and setter of the stack list. This is done in the
InventoryAccessors class. An inventory implementing LithiumInventory allows hoppers to use the LithiumStackList's
additional data for some optimizations.

## Inventory Caching

Hoppers try to cache the inventory they are interacting with. If there is a cached inventory it can be used directly
without querying the world again. This is possible for BlockEntities, Composters and no block inventory presence. The
cache can be validated by checking whether the BlockEntity has not been removed from the world. Composters and no block
inventory presence will be invalidated when the hopper receives a block update from the corresponding direction. A
workaround for the update suppressing behavior when placing a hopper in a powered position
(cf. https://www.youtube.com/watch?v=QVOONJ1OY44 ) is included (cf. HopperBlockMixin). Entity inventories (storage
minecarts) are not cached, as they are randomly chosen every tick, may move or be destroyed at any time without notice
to the hopper.

## Modification Counter Shortcuts and Comparator Updates

Hoppers keep track of the last inventory they interacted with, and its modification count, even if the inventory is an
entity which must not be cached. This data can still be used for shortcutting the item transfer, given the hopper
interacts with the same inventory again. If both the hopper the inventory the hopper interacts with were not modified
since the last failed transfer attempt, it is known that the current attempt will fail as well. Therefore, the whole
transfer logic can be skipped, however, the side effects (comparator updates) of the failed transfer have to be
mimicked. Hoppers use the inventory modification counters to determine whether the transfer attempt can be skipped. The
comparator side effects (ComparatorUpdatePattern) are computed in HopperHelper and cached in the LithiumStackList. The
side effect cache is invalidated when the inventory is modified. The ComparatorUpdatePattern is only guaranteed to be
calculated like vanilla if mods do not add item types with a non-power-of-two maximum stack size.

The inventory modification counters are updated in LithiumStackList and ItemStackMixin. ItemStacks keep a reference to
the LithiumStackList they are in, and they notify the LithiumStackList when their stack size is manipulated. ItemStacks
can only be in one inventory at a time.

## Entity Interaction Shortcuts

The entity tracker engine makes each entity section (16x16x16 blocks) keep track on the last tick time an Entity of
class Inventory or ItemEntity have moved, appeared or disappeared inside them. Hoppers use the modification tick time to
determine whether any ItemEntity may have entered the pickup area after the last failed item pickup attempt. If the
hopper inventory did not change after the last failed item pickup attempt and no ItemEntities moved, the item pickup
attempt can be skipped, as it is guaranteed to fail like the previous attempt. Similarly, if there was no Inventory
Entity present last time, and no Inventory Entities have moved or appeared, the hopper does not need to search for
Inventory Entities.

## Further Development

These ideas are not implemented in Lithium so far:

Cache whether any comparator can be updated from a given inventory. This could be used to skip the comparator updates.
This might be hard to do over chunk boundaries, but the inner 12x12 of a chunk is probably safe.

Cache whether a LithiumStackList is empty or full. Do not forget directional behavior of SidedInventories. This could
speed up the empty and full inventory checks.
