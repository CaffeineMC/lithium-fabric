package net.caffeinemc.mods.lithium.common.entity.item;

import net.caffeinemc.mods.lithium.mixin.util.accessors.ItemEntityAccessor;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * This LazyIterationConsumer collects item entities that the searching item entity can merge with.
 * The merging operation itself is NOT performed, as this would cause ConcurrentModificationException.
 * Instead, the merging operations are simulated to determine a list of item entities the merging operation
 * will be successful with.
 */
public class ItemEntityLazyIterationConsumer implements AbortableIterationConsumer<ItemEntity> {
    private final ItemStack stack;
    private final AABB box;
    private final Predicate<ItemEntity> predicate;
    private final ArrayList<ItemEntity> mergeEntities;
    private final ItemEntity searchingEntity;
    private int adjustedStackCount;

    public ItemEntityLazyIterationConsumer(ItemEntity searchingEntity, AABB box, Predicate<ItemEntity> predicate) {
        this.searchingEntity = searchingEntity;
        this.box = box;
        this.predicate = predicate;
        this.mergeEntities = new ArrayList<>();
        this.stack = this.searchingEntity.getItem();
        this.adjustedStackCount = this.stack.getCount();
    }

    public ArrayList<ItemEntity> getMergeEntities() {
        return this.mergeEntities;
    }

    @Override
    public Continuation accept(ItemEntity otherItemEntity) {
        if (!this.box.intersects(otherItemEntity.getBoundingBox()) || !this.predicate.test(otherItemEntity)) {
            return Continuation.CONTINUE;
        }
        //We have to finish the iteration of the entity lists before we can start modifying or removing the entities.
        // This is why we dry-run the merging operation, and then only actually merge later.

        int receivedItemCount = predictReceivedItemCount(this.searchingEntity, this.stack, this.adjustedStackCount, otherItemEntity);
        if (receivedItemCount != 0) {
            //The item entity can be merged, so add it to the list of entities to merge with and adjust the stack count.
            this.mergeEntities.add(otherItemEntity);
            this.adjustedStackCount += receivedItemCount;

            if (this.adjustedStackCount <= 0 || this.adjustedStackCount >= this.stack.getMaxStackSize()) {
                return Continuation.ABORT;
            }
        }

        return Continuation.CONTINUE;
    }

    /**
     * This method is a copies the merging logic from ItemEntity but without applying the changes.
     * Here the number of items transferred between the two item entities is calculated after
     * the two item entities have been determined to be mergeable.
     */
    private static int predictReceivedItemCount(ItemEntity thisEntity, ItemStack thisStack, int adjustedStackCount, ItemEntity otherEntity) {
        ItemStack otherStack;
        if (Objects.equals(((ItemEntityAccessor) thisEntity).lithium$getOwner(), ((ItemEntityAccessor) otherEntity).lithium$getOwner())
                && ItemEntity.areMergable(thisStack, otherStack = otherEntity.getItem())) {
            if (otherStack.getCount() < adjustedStackCount) {
                    return getTransferAmount(thisStack.getMaxStackSize(), adjustedStackCount, otherStack.getCount());
//                return otherStack.getCount(); //This would make sense, but vanilla actually limits the transfer to 64 items.
                // The itemEntity.canMerge call above ensures that in normal circumstances the entire stack is transferred.
                // When the stack has a custom max stack size > 64, at most 64 items are transferred.
            } else {
                    return -getTransferAmount(otherStack.getMaxStackSize(), otherStack.getCount(), adjustedStackCount);
//                return -adjustedStackCount;
            }
        }
        return 0;
    }

    /**
     * This method is a copies the merging logic from ItemEntity but without applying the changes.
     * Here the number of items transferred between the two item entities is calculated.
     */
    private static int getTransferAmount(int maxCount, int targetStackCount, int sourceStackCount) {
        return Math.min(Math.min(maxCount, 64) - targetStackCount, sourceStackCount);
    }
}
