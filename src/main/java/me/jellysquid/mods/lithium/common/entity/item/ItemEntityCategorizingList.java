package me.jellysquid.mods.lithium.common.entity.item;

import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import me.jellysquid.mods.lithium.common.hopper.NotifyingItemStack;
import me.jellysquid.mods.lithium.mixin.util.accessors.ItemStackAccessor;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class ItemEntityCategorizingList extends AbstractList<ItemEntity> {
    public static final int ITEM_ENTITY_CATEGORIZATION_THRESHOLD = 20;
    public static final int ITEM_ENTITY_CATEGORIZATION_THRESHOLD_2 = 20;

    private final ArrayList<ItemEntity> delegate;
    private Reference2ReferenceOpenHashMap<Item, List<ItemEntity>> itemEntitiesByItem;

    public ItemEntityCategorizingList(ArrayList<ItemEntity> delegate) {
        this.delegate = delegate;
    }

    public List<ItemEntity> getItemEntitiesForMerge(ItemEntity searchingEntity) {
        if (this.itemEntitiesByItem == null) {
            this.itemEntitiesByItem = new Reference2ReferenceOpenHashMap<>();
            for (ItemEntity entity : this.delegate) {
                this.itemEntitiesByItem.computeIfAbsent(((ItemStackAccessor) (Object) entity.getStack()).lithium$getItem(), item -> new ArrayList<>()).add(entity);
            }
        }

        Item item = ((ItemStackAccessor) (Object) searchingEntity.getStack()).lithium$getItem();
        List<ItemEntity> itemEntities = this.itemEntitiesByItem.get(item);
        if (itemEntities == null) {
            return Collections.emptyList();
        }

        // If there are enough item entities in one category, divide the item entities into 3 buckets:
        // Stacks that are more than 50% full can only merge with stacks that are less than 50% full, etc.
        // Buckets:        0           1            2
        // Content:   "<100% full" "<=50% full"   "any"
        // Search if: "< 50% full" ">=50% full"   "none"

        if (itemEntities instanceof ArrayList<ItemEntity> itemEntityArrayList && itemEntities.size() > ITEM_ENTITY_CATEGORIZATION_THRESHOLD_2) {
            SizeBucketedItemEntityList itemEntitiesBucketed = new SizeBucketedItemEntityList(itemEntityArrayList);
            this.itemEntitiesByItem.put(item, itemEntitiesBucketed);
            itemEntities = itemEntitiesBucketed;
        }

        if (itemEntities instanceof SizeBucketedItemEntityList itemEntitiesBucketed) {
            if (itemEntities.size() >= ITEM_ENTITY_CATEGORIZATION_THRESHOLD_2 / 2) {
                return itemEntitiesBucketed.getSearchGroup(searchingEntity);
            }

            itemEntities = itemEntitiesBucketed.downgradeToArrayList();
            this.itemEntitiesByItem.put(item, itemEntities);
        }

        return itemEntities;
    }

    public void handleItemEntityStackReplacement(ItemEntity itemEntity, ItemStack oldStack) {
        if (this.itemEntitiesByItem != null) {
            Item oldItem = ((ItemStackAccessor) (Object) oldStack).lithium$getItem();
            List<ItemEntity> oldCategoryList = this.itemEntitiesByItem.get(oldItem);
            if (oldCategoryList != null) {
                if (oldCategoryList instanceof ItemEntityCategorizingList.SizeBucketedItemEntityList bucketedList) {
                    bucketedList.removeOld(itemEntity, oldStack); //We need to use the old stack to know which buckets to remove from
                }
                oldCategoryList.remove(itemEntity);
            }

            Item newItem = ((ItemStackAccessor) (Object) itemEntity.getStack()).lithium$getItem();
            List<ItemEntity> newCategoryList = this.itemEntitiesByItem.computeIfAbsent(newItem, item -> new ArrayList<>());
            //Use binary search in delegate to find the correct position according to the main list's sorting
            insertUsingBinarySearchAccordingToOrderIndex(newCategoryList, itemEntity);
        }
    }

    public static <T> void insertUsingBinarySearchAccordingToOrderIndex(List<T> list, T element) {
        int index = Collections.binarySearch(list, element,
            Comparator.comparingLong(element2 -> ((ItemEntityOrderInternalAccess) element2).lithium$getOrderIndex()));
        if (index >= 0) {
            throw new IllegalStateException("Element is already in the list!");
        }
        index = - (index + 1); //Get insertion location according to Collections.binarySearch
        list.add(index, element);
    }

    public ArrayList<ItemEntity> getDelegate() {
        this.itemEntitiesByItem = null;
        return this.delegate;
    }


    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    @NotNull
    @Override
    public Iterator<ItemEntity> iterator() {
        this.itemEntitiesByItem = null;
        return delegate.iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        return delegate.toArray(a);
    }

    @Override
    public boolean add(ItemEntity itemEntity) {
        if (this.itemEntitiesByItem != null) {
            Item category = ((ItemStackAccessor) (Object) itemEntity.getStack()).lithium$getItem();
            this.itemEntitiesByItem.computeIfAbsent(category, item -> new ArrayList<>()).add(itemEntity);
        }
        return delegate.add(itemEntity);
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof ItemEntity itemEntity && this.itemEntitiesByItem != null) {
            Item category = ((ItemStackAccessor) (Object) itemEntity.getStack()).lithium$getItem();

            List<ItemEntity> itemEntities = this.itemEntitiesByItem.get(category);
            if (itemEntities != null) {
                itemEntities.remove(itemEntity);
            }
        }
        return delegate.remove(o);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return delegate.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends ItemEntity> c) {
        for (ItemEntity itemEntity : c) {
            this.add(itemEntity);
        }
        return delegate.addAll(c);
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends ItemEntity> c) {
        this.itemEntitiesByItem = null;
        return delegate.addAll(index, c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        this.itemEntitiesByItem = null;
        return delegate.removeAll(c);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        this.itemEntitiesByItem = null;
        return delegate.retainAll(c);
    }

    @Override
    public void replaceAll(UnaryOperator<ItemEntity> operator) {
        this.itemEntitiesByItem = null;
        delegate.replaceAll(operator);
    }

    @Override
    public void sort(Comparator<? super ItemEntity> c) {
        this.itemEntitiesByItem = null;
        delegate.sort(c);
    }

    @Override
    public void clear() {
        this.itemEntitiesByItem = null;
        delegate.clear();
    }

    @Override
    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public ItemEntity get(int index) {
        return delegate.get(index);
    }

    @Override
    public ItemEntity set(int index, ItemEntity element) {
        this.itemEntitiesByItem = null;
        return delegate.set(index, element);
    }

    @Override
    public void add(int index, ItemEntity element) {
        this.itemEntitiesByItem = null;
        delegate.add(index, element);
    }

    @Override
    public ItemEntity remove(int index) {
        ItemEntity remove = delegate.remove(index);
        if (this.itemEntitiesByItem != null) {
            Item category = ((ItemStackAccessor) (Object) remove.getStack()).lithium$getItem();
            List<ItemEntity> itemEntities = this.itemEntitiesByItem.get(category);
            if (itemEntities != null) {
                itemEntities.remove(remove);
            }
        }
        return remove;
    }

    @Override
    public int indexOf(Object o) {
        return delegate.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return delegate.lastIndexOf(o);
    }

    @NotNull
    @Override
    public ListIterator<ItemEntity> listIterator() {
        this.itemEntitiesByItem = null;
        return delegate.listIterator();
    }

    @NotNull
    @Override
    public ListIterator<ItemEntity> listIterator(int index) {
        this.itemEntitiesByItem = null;
        return delegate.listIterator(index);
    }

    @NotNull
    @Override
    public List<ItemEntity> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Spliterator<ItemEntity> spliterator() {
        return delegate.spliterator();
    }

    @Override
    public <T> T[] toArray(IntFunction<T[]> generator) {
        return delegate.toArray(generator);
    }

    @Override
    public boolean removeIf(Predicate<? super ItemEntity> filter) {
        this.itemEntitiesByItem = null;
        return delegate.removeIf(filter);
    }

    @Override
    public Stream<ItemEntity> stream() {
        return delegate.stream();
    }

    @Override
    public Stream<ItemEntity> parallelStream() {
        return delegate.parallelStream();
    }

    @Override
    public void forEach(Consumer<? super ItemEntity> action) {
        delegate.forEach(action);
    }

    // If there are enough item entities in one category, divide the item entities into 3 buckets:
    // Stacks that are more than 50% full can only merge with stacks that are less than 50% full, etc.
    // Buckets:        0           1            2
    // Content:   "<100% full" "<=50% full"   "any"
    // Search if: "< 50% full" ">=50% full"   "none"
    class SizeBucketedItemEntityList extends AbstractList<ItemEntity> {

        private final ArrayList<ItemEntity> allMergeableList;
        private final ArrayList<ItemEntity> selfMergeableList;
        private final ArrayList<ItemEntity> delegate;
        private long currentOrderIndex = 0;

        private final Reference2ReferenceOpenHashMap<ItemEntity, ItemStackSubscriber> subscribers;

        public SizeBucketedItemEntityList(ArrayList<ItemEntity> delegate) {
            this.allMergeableList = new ArrayList<>();
            this.selfMergeableList = new ArrayList<>();
            this.delegate = delegate;
            this.subscribers = new Reference2ReferenceOpenHashMap<>();
            for (ItemEntity itemEntity : this.delegate) {
                this.updateStackSubscriptionOnAdd(itemEntity);
                this.addToGroups(itemEntity);
                this.setOrderIndex(itemEntity);
            }
        }

        public List<ItemEntity> getSearchGroup(ItemEntity searchingEntity) {
            Item item = ((ItemStackAccessor) (Object) searchingEntity.getStack()).lithium$getItem();
            ItemStack stack = searchingEntity.getStack();
            int count = stack.getCount();
            int maxCount = item.getMaxCount();
            if (count * 2 >= maxCount) { //>=50% full
                return this.selfMergeableList;
            }
            return this.allMergeableList;
        }

        ArrayList<ItemEntity> downgradeToArrayList() {
            return this.delegate;
        }

        private void addToGroups(ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getStack();
            int count = stack.getCount();
            int maxCount = stack.getMaxCount();
            if (count < maxCount) { //<100% full
                this.allMergeableList.add(itemEntity);
            }
            if (count * 2 <= maxCount) { //<=50% full
                this.selfMergeableList.add(itemEntity);
            }
        }

        private void removeFromGroups(ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getStack();
            int count = stack.getCount();
            int maxCount = stack.getMaxCount();
            if (count < maxCount) { //<100% full
                this.allMergeableList.remove(itemEntity);
            }
            if (count * 2 <= maxCount) { //<=50% full
                this.selfMergeableList.remove(itemEntity);
            }
        }

        private void addToGroupsUsingBinarySearch(ItemEntity element) {
            ItemStack stack = element.getStack();
            int count = stack.getCount();
            int maxCount = stack.getMaxCount();
            if (count < maxCount) { //<100% full
                insertUsingBinarySearchAccordingToOrderIndex(this.allMergeableList, element);
            }
            if (count * 2 <= maxCount) { //<=50% full
                insertUsingBinarySearchAccordingToOrderIndex(this.selfMergeableList, element);
            }
        }

        private void updateStackSubscriptionOnAdd(ItemEntity element) {
            SizeBucketedItemEntityList itemEntities = this;
            ItemStackSubscriber subscriber = new ItemStackSubscriber() {
                @Override
                public void lithium$notifyBeforeCountChange(ItemStack itemStack, int slot, int newCount) {
                    itemEntities.notifyBeforeCountChange(element, newCount);
                }

                @Override
                public void lithium$notifyItemEntityStackSwap(ItemEntity itemEntity, ItemStack oldStack) {
                    itemEntities.notifyStackSwap(itemEntity, oldStack);
                }
            };
            this.subscribers.put(element, subscriber);
            ((NotifyingItemStack) (Object) element.getStack()).lithium$subscribe(subscriber);
        }

        private void updateStackSubscriptionOnRemove(ItemEntity element) {
            ItemStackSubscriber subscriber = this.subscribers.remove(element);
            ((NotifyingItemStack) (Object) element.getStack()).lithium$unsubscribe(subscriber);
        }

        private void notifyStackSwap(ItemEntity itemEntity, ItemStack oldStack) {
            ItemEntityCategorizingList.this.handleItemEntityStackReplacement(itemEntity, oldStack);
        }

        private void setOrderIndex(ItemEntity itemEntity) {
            ((ItemEntityOrderInternalAccess) itemEntity).lithium$setOrderIndex(currentOrderIndex++);
        }

        public void notifyBeforeCountChange(ItemEntity itemEntity, int newCount) {
            ItemStack itemStack = itemEntity.getStack();
            int oldCount = itemStack.getCount();
            int maxCount = itemStack.getMaxCount();

            if (oldCount < maxCount && !(newCount < maxCount)) { //no longer <100% full
                this.allMergeableList.remove(itemEntity);
            } else if (!(oldCount < maxCount) && newCount < maxCount) { //<100% full from now on
                insertUsingBinarySearchAccordingToOrderIndex(this.allMergeableList, itemEntity);
            }
            if (oldCount * 2 <= maxCount && !(newCount * 2 <= maxCount)) { //no longer <=50% full
                this.selfMergeableList.remove(itemEntity);
            } else if (!(oldCount * 2 <= maxCount) && newCount * 2 <= maxCount) { //<=50% full from now on
                insertUsingBinarySearchAccordingToOrderIndex(this.selfMergeableList, itemEntity);
            }
        }

        public void removeOld(ItemEntity itemEntity, ItemStack oldStack) {
            if (this.delegate.remove(itemEntity)) {
                this.updateStackSubscriptionOnRemove(itemEntity);
                int count = oldStack.getCount();
                int maxCount = oldStack.getMaxCount();
                if (count < maxCount) { //<100% full
                    this.allMergeableList.remove(itemEntity);
                }
                if (count * 2 <= maxCount) { //<=50% full
                    this.selfMergeableList.remove(itemEntity);
                }
            }
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return delegate.contains(o);
        }

        @NotNull
        @Override
        public Iterator<ItemEntity> iterator() {
            return Iterators.unmodifiableIterator(delegate.iterator());
        }

        @NotNull
        @Override
        public Object[] toArray() {
            return delegate.toArray();
        }

        @NotNull
        @Override
        public <T> T[] toArray(@NotNull T[] a) {
            return delegate.toArray(a);
        }

        @Override
        public boolean add(ItemEntity itemEntity) {
            this.updateStackSubscriptionOnAdd(itemEntity);
            this.addToGroups(itemEntity);
            this.setOrderIndex(itemEntity);
            return this.delegate.add(itemEntity);
        }

        @Override
        public boolean remove(Object o) {
            if (o instanceof ItemEntity itemEntity && this.delegate.remove(itemEntity)) {
                this.updateStackSubscriptionOnRemove(itemEntity);
                this.removeFromGroups(itemEntity);
                return true;
            }
            return false;
        }

        @Override
        public boolean containsAll(@NotNull Collection<?> c) {
            return delegate.containsAll(c);
        }

        @Override
        public boolean addAll(@NotNull Collection<? extends ItemEntity> c) {
            boolean b = false;
            for (ItemEntity itemEntity : c) {
                b |= this.add(itemEntity);
            }
            return b;
        }

        @Override
        public boolean equals(Object o) {
            return delegate.equals(o);
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public ItemEntity get(int index) {
            return delegate.get(index);
        }

        @Override
        public ItemEntity set(int index, ItemEntity element) {
            ItemEntity prev = delegate.set(index, element);
            if (prev != element) {
                this.updateStackSubscriptionOnRemove(prev);
                this.removeFromGroups(prev);
                this.updateStackSubscriptionOnAdd(element);
                this.addToGroupsUsingBinarySearch(element);
            }
            return prev;

        }

        @Override
        public void add(int index, ItemEntity element) {
            this.delegate.add(index, element);
            this.updateStackSubscriptionOnAdd(element);
            this.addToGroupsUsingBinarySearch(element);
        }

        @Override
        public ItemEntity remove(int index) {
            ItemEntity remove = delegate.remove(index);
            if (remove != null) {
                this.updateStackSubscriptionOnRemove(remove);
                this.removeFromGroups(remove);
            }
            return remove;
        }

        @Override
        public int indexOf(Object o) {
            return delegate.indexOf(o);
        }

        @Override
        public int lastIndexOf(Object o) {
            return delegate.lastIndexOf(o);
        }

        @Override
        public <T> T[] toArray(IntFunction<T[]> generator) {
            return delegate.toArray(generator);
        }

        @Override
        public Stream<ItemEntity> stream() {
            return delegate.stream();
        }

        @Override
        public Stream<ItemEntity> parallelStream() {
            return delegate.parallelStream();
        }

        @Override
        public void forEach(Consumer<? super ItemEntity> action) {
            delegate.forEach(action);
        }
    }
}
