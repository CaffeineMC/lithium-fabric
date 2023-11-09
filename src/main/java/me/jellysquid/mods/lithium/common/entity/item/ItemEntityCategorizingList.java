package me.jellysquid.mods.lithium.common.entity.item;

import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
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
    private Reference2IntOpenHashMap<ItemEntity> entityToIndex;
    private int nextIndex = 0;


    public ItemEntityCategorizingList(ArrayList<ItemEntity> delegate) {
        this.delegate = delegate;
    }

    private void initializeItemEntitiesByItem() {
        this.itemEntitiesByItem = new Reference2ReferenceOpenHashMap<>();
        this.entityToIndex = new Reference2IntOpenHashMap<>(this.delegate.size());
        this.nextIndex = 0;
        for (ItemEntity entity : this.delegate) {
            this.itemEntitiesByItem.computeIfAbsent(((ItemStackAccessor) (Object) entity.getStack()).lithium$getItem(), item -> new ArrayList<>()).add(entity);
            this.entityToIndex.put(entity, this.nextIndex++);
        }
    }

    public List<ItemEntity> getItemEntitiesForMerge(ItemEntity searchingEntity) {
        if (this.itemEntitiesByItem == null) {
            this.initializeItemEntitiesByItem();
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
            return itemEntitiesBucketed.getSearchGroup(searchingEntity);
        }

        return itemEntities;
    }

    private void downgradeList(Item item, SizeBucketedItemEntityList itemEntities) {
        List<ItemEntity> itemEntitiesInMap = this.itemEntitiesByItem.get(item);
        if (itemEntities != itemEntitiesInMap) {
            throw new IllegalStateException("List is not the same as the one in the map!");
        }
        if (itemEntities.isEmpty()) {
            this.itemEntitiesByItem.remove(item);
            return;
        }
        itemEntitiesInMap = itemEntities.downgradeToArrayList();
        this.itemEntitiesByItem.put(item, itemEntitiesInMap);
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
            insertUsingBinarySearchAccordingToOrder(newCategoryList, itemEntity, this.entityToIndex);
        }
    }

    public static void insertUsingBinarySearchAccordingToOrder(List<ItemEntity> list, ItemEntity element, Reference2IntOpenHashMap<ItemEntity> order) {
        //Use binary search in delegate to find the correct position according to the main list's sorting
        int index = Collections.binarySearch(list, element, Comparator.comparingInt(order::getInt));
        if (index >= 0) {
            throw new IllegalStateException("Element is already in the list!");
        }
        index = - (index + 1); //Get insertion location according to Collections.binarySearch
        list.add(index, element);
    }

    public ArrayList<ItemEntity> getDelegate() {
        this.resetItemEntitiesByItem();
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
        this.resetItemEntitiesByItem();
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

            this.entityToIndex.put(itemEntity, this.nextIndex++);
            if (this.nextIndex < 0) {
                this.resetItemEntitiesByItem();
            }
        }
        return delegate.add(itemEntity);
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof ItemEntity itemEntity && this.itemEntitiesByItem != null) {
            removeInternal(itemEntity);
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
        this.resetItemEntitiesByItem();
        return delegate.addAll(index, c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        this.resetItemEntitiesByItem();
        return delegate.removeAll(c);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        this.resetItemEntitiesByItem();
        return delegate.retainAll(c);
    }

    @Override
    public void replaceAll(UnaryOperator<ItemEntity> operator) {
        this.resetItemEntitiesByItem();
        delegate.replaceAll(operator);
    }

    @Override
    public void sort(Comparator<? super ItemEntity> c) {
        this.resetItemEntitiesByItem();
        delegate.sort(c);
    }

    @Override
    public void clear() {
        this.resetItemEntitiesByItem();
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
        this.resetItemEntitiesByItem();
        return delegate.set(index, element);
    }

    @Override
    public void add(int index, ItemEntity element) {
        this.resetItemEntitiesByItem();
        delegate.add(index, element);
    }

    @Override
    public ItemEntity remove(int index) {
        ItemEntity remove = delegate.remove(index);
        if (this.itemEntitiesByItem != null) {
            removeInternal(remove);
        }
        return remove;
    }

    private void removeInternal(ItemEntity remove) {
        Item category = ((ItemStackAccessor) (Object) remove.getStack()).lithium$getItem();
        List<ItemEntity> itemEntities = this.itemEntitiesByItem.get(category);
        if (itemEntities != null) {
            itemEntities.remove(remove);
            if (itemEntities.isEmpty()) {
                this.itemEntitiesByItem.remove(category);
            }
        }
        this.entityToIndex.removeInt(remove);
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
        this.resetItemEntitiesByItem();
        return delegate.listIterator();
    }

    @NotNull
    @Override
    public ListIterator<ItemEntity> listIterator(int index) {
        this.resetItemEntitiesByItem();
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
        this.resetItemEntitiesByItem();
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

    private void resetItemEntitiesByItem() {
        this.itemEntitiesByItem = null;
        this.entityToIndex = null;
    }

    // If there are enough item entities in one category, divide the item entities into 3 buckets:
    // Stacks that are more than 50% full can only merge with stacks that are less than 50% full, etc.
    // Buckets:        0           1            2
    // Content:   "<100% full" "<=50% full"   "any"
    // Search if: "< 50% full" ">=50% full"   "none"
    class SizeBucketedItemEntityList extends AbstractList<ItemEntity> {

        private final ArrayList<ItemEntity> notFullEntities;
        private final ArrayList<ItemEntity> maxHalfFullEntities;
        private final ArrayList<ItemEntity> entities;
        private final Reference2ReferenceOpenHashMap<ItemEntity, ItemStackSubscriber> subscribers;

        public SizeBucketedItemEntityList(ArrayList<ItemEntity> entities) {
            this.notFullEntities = new ArrayList<>();
            this.maxHalfFullEntities = new ArrayList<>();
            this.entities = entities;
            this.subscribers = new Reference2ReferenceOpenHashMap<>();
            for (ItemEntity itemEntity : this.entities) {
                this.updateStackSubscriptionOnAdd(itemEntity);
                this.addToGroups(itemEntity);
            }
        }

        public List<ItemEntity> getSearchGroup(ItemEntity searchingEntity) {
            Item item = ((ItemStackAccessor) (Object) searchingEntity.getStack()).lithium$getItem();
            ItemStack stack = searchingEntity.getStack();
            int count = stack.getCount();
            int maxCount = item.getMaxCount();
            if (count * 2 >= maxCount) { //>=50% full
                return this.maxHalfFullEntities;
            }
            return this.notFullEntities;
        }

        ArrayList<ItemEntity> downgradeToArrayList() {
            return this.entities;
        }

        private void addToGroups(ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getStack();
            int count = stack.getCount();
            int maxCount = stack.getMaxCount();
            if (count < maxCount) { //<100% full
                this.notFullEntities.add(itemEntity);
            }
            if (count * 2 <= maxCount) { //<=50% full
                this.maxHalfFullEntities.add(itemEntity);
            }
        }

        private void removeFromGroups(ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getStack();
            int count = stack.getCount();
            int maxCount = stack.getMaxCount();
            if (count < maxCount) { //<100% full
                this.notFullEntities.remove(itemEntity);
            }
            if (count * 2 <= maxCount) { //<=50% full
                this.maxHalfFullEntities.remove(itemEntity);
            }
        }

        private void addToGroupsUsingBinarySearch(ItemEntity element) {
            ItemStack stack = element.getStack();
            int count = stack.getCount();
            int maxCount = stack.getMaxCount();
            if (count < maxCount) { //<100% full
                insertUsingBinarySearchAccordingToOrder(this.notFullEntities, element, ItemEntityCategorizingList.this.entityToIndex);
            }
            if (count * 2 <= maxCount) { //<=50% full
                insertUsingBinarySearchAccordingToOrder(this.maxHalfFullEntities, element, ItemEntityCategorizingList.this.entityToIndex);
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

        public void notifyBeforeCountChange(ItemEntity itemEntity, int newCount) {
            ItemStack itemStack = itemEntity.getStack();
            int oldCount = itemStack.getCount();
            int maxCount = itemStack.getMaxCount();

            if (oldCount < maxCount && !(newCount < maxCount)) { //no longer <100% full
                this.notFullEntities.remove(itemEntity);
            } else if (!(oldCount < maxCount) && newCount < maxCount) { //<100% full from now on
                insertUsingBinarySearchAccordingToOrder(this.notFullEntities, itemEntity, ItemEntityCategorizingList.this.entityToIndex);
            }
            if (oldCount * 2 <= maxCount && !(newCount * 2 <= maxCount)) { //no longer <=50% full
                this.maxHalfFullEntities.remove(itemEntity);
            } else if (!(oldCount * 2 <= maxCount) && newCount * 2 <= maxCount) { //<=50% full from now on
                insertUsingBinarySearchAccordingToOrder(this.maxHalfFullEntities, itemEntity, ItemEntityCategorizingList.this.entityToIndex);
            }
        }

        public void removeOld(ItemEntity itemEntity, ItemStack oldStack) {
            if (this.entities.remove(itemEntity)) {
                this.updateStackSubscriptionOnRemove(itemEntity);
                int count = oldStack.getCount();
                int maxCount = oldStack.getMaxCount();
                if (count < maxCount) { //<100% full
                    this.notFullEntities.remove(itemEntity);
                }
                if (count * 2 <= maxCount) { //<=50% full
                    this.maxHalfFullEntities.remove(itemEntity);
                }

                this.checkResizeOnRemoval(oldStack);
            }
        }

        @Override
        public int size() {
            return entities.size();
        }

        @Override
        public boolean isEmpty() {
            return entities.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return entities.contains(o);
        }

        @NotNull
        @Override
        public Iterator<ItemEntity> iterator() {
            return Iterators.unmodifiableIterator(entities.iterator());
        }

        @NotNull
        @Override
        public Object[] toArray() {
            return entities.toArray();
        }

        @NotNull
        @Override
        public <T> T[] toArray(@NotNull T[] a) {
            return entities.toArray(a);
        }

        @Override
        public boolean add(ItemEntity itemEntity) {
            this.updateStackSubscriptionOnAdd(itemEntity);
            this.addToGroups(itemEntity);
            return this.entities.add(itemEntity);
        }

        @Override
        public boolean remove(Object o) {
            if (o instanceof ItemEntity itemEntity && this.entities.remove(itemEntity)) {
                this.updateStackSubscriptionOnRemove(itemEntity);
                this.removeFromGroups(itemEntity);
                this.checkResizeOnRemoval(itemEntity);
                return true;
            }
            return false;
        }

        @Override
        public boolean containsAll(@NotNull Collection<?> c) {
            return entities.containsAll(c);
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
            return entities.equals(o);
        }

        @Override
        public int hashCode() {
            return entities.hashCode();
        }

        @Override
        public ItemEntity get(int index) {
            return entities.get(index);
        }

        @Override
        public ItemEntity set(int index, ItemEntity element) {
            ItemEntity prev = entities.set(index, element);
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
            this.entities.add(index, element);
            this.updateStackSubscriptionOnAdd(element);
            this.addToGroupsUsingBinarySearch(element);
        }

        @Override
        public ItemEntity remove(int index) {
            ItemEntity remove = entities.remove(index);
            if (remove != null) {
                this.updateStackSubscriptionOnRemove(remove);
                this.removeFromGroups(remove);
                this.checkResizeOnRemoval(remove);
            }
            return remove;
        }

        private void checkResizeOnRemoval(ItemEntity removed) {
            int size = this.entities.size();
            if (size < ITEM_ENTITY_CATEGORIZATION_THRESHOLD_2 / 2 ) {
                ItemEntityCategorizingList.this.downgradeList(((ItemStackAccessor) (Object) removed.getStack()).lithium$getItem(), this);
            }
        }
        private void checkResizeOnRemoval(ItemStack removed) {
            int size = this.entities.size();
            if (size < ITEM_ENTITY_CATEGORIZATION_THRESHOLD_2 / 2 ) {
                ItemEntityCategorizingList.this.downgradeList(((ItemStackAccessor) (Object) removed).lithium$getItem(), this);
            }
        }

        @Override
        public int indexOf(Object o) {
            return entities.indexOf(o);
        }

        @Override
        public int lastIndexOf(Object o) {
            return entities.lastIndexOf(o);
        }

        @Override
        public <T> T[] toArray(IntFunction<T[]> generator) {
            return entities.toArray(generator);
        }

        @Override
        public Stream<ItemEntity> stream() {
            return entities.stream();
        }

        @Override
        public Stream<ItemEntity> parallelStream() {
            return entities.parallelStream();
        }

        @Override
        public void forEach(Consumer<? super ItemEntity> action) {
            entities.forEach(action);
        }
    }
}
