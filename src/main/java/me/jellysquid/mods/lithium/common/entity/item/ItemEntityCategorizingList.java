package me.jellysquid.mods.lithium.common.entity.item;

import it.unimi.dsi.fastutil.Hash;
import me.jellysquid.mods.lithium.common.util.change_tracking.ChangePublisher;
import me.jellysquid.mods.lithium.common.util.change_tracking.ChangeSubscriber;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.function.LazyIterationConsumer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class ItemEntityCategorizingList extends ElementCategorizingList<ItemEntity, ItemStack> implements ChangeSubscriber.CountChangeSubscriber<ItemEntity> {
//TODO use ItemStack.getMaxCount instead of Item.getMaxCount, e.g. for carpet mod compatibility (stackable empty shulker boxes)
    @SuppressWarnings("unused")
    public static final int DOWNGRADE_THRESHOLD = 10; //TODO implement downgrade
    public static final int UPGRADE_THRESHOLD = 20;

    private static final Hash.Strategy<ItemStack> ITEM_STACK_STRATEGY = new Hash.Strategy<>() {
        @Override
        public int hashCode(ItemStack itemStack) {
            return ItemStack.hashCode(itemStack);
        }

        @Override
        public boolean equals(ItemStack itemStack, ItemStack otherItemStack) {
            return ItemStack.areItemsAndComponentsEqual(itemStack, otherItemStack);
        }
    };

    public static ItemEntityCategorizingList wrapDelegate(ArrayList<ItemEntity> delegate) {
        ItemEntityCategorizingList itemEntities = new ItemEntityCategorizingList(delegate);
        itemEntities.initialize();
        return itemEntities;
    }
    private ItemEntityCategorizingList(ArrayList<ItemEntity> delegate) {
        super(delegate, ITEM_STACK_STRATEGY);
    }

    @Override
    ItemStack getCategory(ItemEntity element) {
        return element.getStack();
    }

    @Override
    boolean areSubcategoriesAlwaysEmpty(ItemStack itemStack) {
        return itemStack.getMaxCount() == 1;
    }

    // If there are enough ItemStack entities in one category, divide the ItemStack entities into 3 buckets:
    // Stacks that are more than 50% full can only merge with stacks that are less than 50% full, etc.
    // Buckets:        A           B            *
    // Content:   "<100% full" "<=50% full"   "any"
    // Search if: "< 50% full" ">=50% full"  otherwise

    @Override
    boolean isSubCategoryA(ItemEntity element) { //<100% full
        ItemStack stack = element.getStack();
        return isSubCategoryA(stack);
    }

    private static boolean isSubCategoryA(ItemStack stack) {
        int count = stack.getCount();
        int maxCount = stack.getMaxCount();
        return isSubCategoryA(count, maxCount);
    }

    private static boolean isSubCategoryA(int count, int maxCount) {
        return count < maxCount;
    }

    @Override
    boolean isSubCategoryB(ItemEntity element) { //<=50% full
        ItemStack stack = element.getStack();
        return isSubCategoryB(stack);
    }

    private static boolean isSubCategoryB(ItemStack stack) {
        int count = stack.getCount();
        int maxCount = (stack).getMaxCount();
        return isSubCategoryB(count, maxCount);
    }

    private static boolean isSubCategoryB(int count, int maxCount) {
        return count * 2 <= maxCount;
    }

    public LazyIterationConsumer.NextIteration consumeForEntityStacking(ItemEntity searchingEntity, LazyIterationConsumer<ItemEntity> itemEntityConsumer) {
        ItemStack stack = searchingEntity.getStack();
        int count = stack.getCount();
        int maxCount = stack.getMaxCount();
        if (count * 2 >= maxCount) { //>=50% full
            return this.consumeCategoryB(itemEntityConsumer, stack); // Entities that are <= 50% full.
        } else {
            return this.consumeCategoryA(itemEntityConsumer, stack); // Entities that are <100% full
        }
    }

    @Override
    void onElementSubcategorized(ItemEntity element, int index) {
        //Sub-categorizing is based on the ItemStack stack count. The collection must be updated whenever the stack count changes.
        //Use the ItemStack stack subscription system to receive updates:

        //noinspection unchecked
        ((ChangePublisher<ItemEntity>) element).lithium$subscribe(this, index);
    }

    @Override
    public void lithium$notifyCount(ItemEntity element, int index, int newCount) {
        //Fix the subcategories the ItemStack is added to
        ItemStack itemStack = this.getCategory(element);

        int maxCount = itemStack.getMaxCount();
        boolean categoryA = isSubCategoryA(newCount, maxCount);
        boolean oldCategoryA = this.isSubCategoryA(element);
        boolean categoryB = isSubCategoryB(newCount, maxCount);
        boolean oldCategoryB = this.isSubCategoryB(element);

        this.updateSubcategoryAssignment(itemStack, index, categoryA, oldCategoryA, categoryB, oldCategoryB);
    }

    private void updateSubcategoryAssignment(ItemStack category, int index, boolean categoryA, boolean oldCategoryA, boolean categoryB, boolean oldCategoryB) {
        if (categoryA == oldCategoryA && categoryB == oldCategoryB) {
            return;
        }
        //Remove element from subcategories if needed
        this.removeFromSubCategories(category, index, oldCategoryA && !categoryA, oldCategoryB && !categoryB, false);
        //Add element to subcategories if needed
        this.addToSubCategories(category, index, categoryA && !oldCategoryA, categoryB && !oldCategoryB, false);
    }


//    private void notifyAfterStackSwap(int index, ItemEntity element, ItemStack oldStack) {
//        ItemStack stack = element.getStack();
//        if (stack == oldStack) {
//            return;
//        }
//        //Fix the stack subscription. We can reuse the subscriber object as it only stores a reference to this list and the element.
//        ItemStackSubscriber subscriber = this.subscribers.get(element);
//        if (subscriber != null) {
//            ((NotifyingItemStack) (Object) oldStack).lithium$unsubscribe(subscriber);
//            ((NotifyingItemStack) (Object) element.getStack()).lithium$subscribeWithIndex(subscriber, index);
//        }
//
//        ItemStack category = this.getCategory(element);
//        //Fix the indices stored in elementsByType, elementsByTypeA and elementsByTypeB.
//        boolean subCategoryA = isSubCategoryA(element);
//        boolean subCategoryB = isSubCategoryB(element);
//
//        boolean prevSubCategoryA = isSubCategoryA(oldStack);
//        boolean prevSubCategoryB = isSubCategoryB(oldStack);
//
//        this.updateCategoryAndSubcategories(category, oldStack, index, subCategoryA, prevSubCategoryA, subCategoryB, prevSubCategoryB);
//
//    }

    @Override
    void onElementUnSubcategorized(ItemEntity element) {
        //noinspection unchecked
        ((ChangePublisher<ItemEntity>) element).lithium$unsubscribe(this);
    }

    @Override
    void onCollectionReset() {
        this.onAllElementsUnSubcategorized();
    }

    @Override
    ItemEntity castOrNull(Object element) {
        return element instanceof ItemEntity ? (ItemEntity) element : null;
    }

    private void onAllElementsUnSubcategorized() {
        for (ItemEntity element : this) {
            //noinspection unchecked
            ((ChangePublisher<ItemEntity>) element).lithium$unsubscribe(this);
        }
    }

    @Override
    public void lithium$notify(@Nullable ItemEntity publisher, int subscriberData) {
        //TODO handle outdated, use subscriberData as index to remove the item until the modification is done
        this.outdated.add(publisher);
    }

    @Override
    public void lithium$forceUnsubscribe(ItemEntity publisher, int subscriberData) {
        //TODO handle outdated, use subscriberData as index to remove the item until the modification is done
        this.outdated.add(publisher);
    }
}
