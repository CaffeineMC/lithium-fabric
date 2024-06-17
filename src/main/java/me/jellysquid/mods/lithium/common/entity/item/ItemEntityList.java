package me.jellysquid.mods.lithium.common.entity.item;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenCustomHashMap;
import me.jellysquid.mods.lithium.common.util.change_tracking.ChangePublisher;
import me.jellysquid.mods.lithium.common.util.change_tracking.ChangeSubscriber;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.function.LazyIterationConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.stream.Stream;

public class ItemEntityList extends AbstractList<ItemEntity> implements ChangeSubscriber.CountChangeSubscriber<ItemEntity> {

    private static final Hash.Strategy<ItemStack> STRATEGY = new Hash.Strategy<>() {
        @Override
        public int hashCode(ItemStack itemStack) {
            return HashCommon.mix(ItemStack.hashCode(itemStack));
        }

        @Override
        public boolean equals(ItemStack itemStack, ItemStack otherItemStack) {
            return itemStack == otherItemStack ||
                    itemStack != null && otherItemStack != null && ItemStack.areItemsAndComponentsEqual(itemStack, otherItemStack);
        }
    };

    public static final int UPGRADE_THRESHOLD = 10;

    private final ArrayList<ItemEntity> delegate;

    //Ordered like delegate, but null elements are either removed or empty elements, while delegate can hold empty elements
    private final ArrayList<ItemEntity> delegateWithNulls;
    private final Object2ReferenceOpenCustomHashMap<ItemStack, IntArrayList> elementsByCategory;
    private final Object2ReferenceOpenCustomHashMap<ItemStack, IntArrayList> maxHalfFullElementsByCategory;
    private final IntOpenHashSet tempUncategorizedElements;

    public ItemEntityList(ArrayList<ItemEntity> delegate) {
        this.delegate = delegate;
        this.delegateWithNulls = new ArrayList<>(delegate);
        this.elementsByCategory = new Object2ReferenceOpenCustomHashMap<>(STRATEGY);
        this.maxHalfFullElementsByCategory = new Object2ReferenceOpenCustomHashMap<>(STRATEGY);;
        this.tempUncategorizedElements = new IntOpenHashSet();

        for (int i = 0; i < this.delegateWithNulls.size(); i++) {
            ItemEntity element = this.delegateWithNulls.get(i);
            this.addToCategories(element, i, false);
            this.subscribeElement(element, i);
        }
    }

    protected void markElementAsOutdated(ItemEntity element, int index) {
        boolean added = this.tempUncategorizedElements.add(index);
        if (added) {
            this.removeFromCategories(element, index);
        }
    }

    protected void processOutdated() {
        if (this.tempUncategorizedElements.isEmpty()) {
            return;
        }

        this.tempUncategorizedElements.forEach((index) -> {
            ItemEntity element = this.delegateWithNulls.get(index);
            if (element != null) {
                if (element.getStack().isEmpty()) {
                    this.delegateWithNulls.set(index, null);
                    this.unsubscribeElement(element);
                } else {
                    this.addToCategories(element, index, true);
                }
            }
        });

        this.tempUncategorizedElements.clear();
    }

    @Override
    public int size() {
        return this.delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return this.delegate.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.delegate.contains(o);
    }

    @NotNull
    @Override
    public Object @NotNull [] toArray() {
        return this.delegate.toArray();
    }

    @NotNull
    @Override
    public <U> U @NotNull [] toArray(U @NotNull [] a) {
        return this.delegate.toArray(a);
    }

    @Override
    public boolean add(ItemEntity element) {
        this.processOutdated();

        if (element.getStack().isEmpty()) {
            this.delegateWithNulls.add(null);
        } else {
            int index = this.delegateWithNulls.size();
            this.delegateWithNulls.add(element);
            this.addToCategories(element, index, false);
            this.subscribeElement(element, index);
        }
        return this.delegate.add(element);
    }

    private void addToCategories(ItemEntity element, int index, boolean insertionSort) {
        ItemStack stack = element.getStack();
        if (stack.isEmpty()) {
            return;
        }

        ItemStack key = addToCategoryList(index, stack, null, this.elementsByCategory, insertionSort);

        if (isMaxHalfFull(stack)) {
            addToCategoryList(index, stack, key, this.maxHalfFullElementsByCategory, insertionSort);
        }
    }

    private static ItemStack addToCategoryList(int index, ItemStack stack, @Nullable ItemStack key, Object2ReferenceOpenCustomHashMap<ItemStack, IntArrayList> categoryMap, boolean insertionSort) {
        if (stack.isEmpty()) {
            return key;
        }
        IntArrayList categoryList = categoryMap.get(stack);
        if (categoryList == null) {
            if (key == null) {
                key = stack.copy(); //Keys must be our own effectively immutable copy
            }

            categoryList = new IntArrayList();
            categoryMap.put(key, categoryList);
        }
        if (insertionSort) {
            //Make sure that the category lists are sorted the same as the delegate list
            int binarySearchIndex = Collections.binarySearch(categoryList, index);
            binarySearchIndex = -(binarySearchIndex + 1); //Get insertion location according to Collections.binarySearch
            categoryList.add(binarySearchIndex, index);
        } else {
            categoryList.add(index);
        }
        return key;
    }

    private void subscribeElement(ItemEntity element, int index) {
        //noinspection unchecked
        ((ChangePublisher<ItemEntity>) element).lithium$subscribe(this, index);
    }

    private static boolean isMaxHalfFull(ItemStack stack) {
        int count = stack.getCount();
        int maxCount = stack.getMaxCount();
        return isMaxHalfFull(count, maxCount);
    }

    private static boolean isMaxHalfFull(int count, int maxCount) {
        return count * 2 <= maxCount;
    }

    @Override
    public boolean remove(Object o) {
        boolean remove = this.delegate.remove(o);
        if (remove && o instanceof ItemEntity element) {
            this.processOutdated();
            this.removeElement(element);
        }
        return remove;
    }

    @Override
    public ItemEntity remove(int index) {
        ItemEntity element = this.delegate.remove(index);
        if (element != null) {
            this.processOutdated();
            this.removeElement(element);
        }

        return element;
    }

    private void removeElement(ItemEntity element) {
        if (!element.getStack().isEmpty()) {
            int index = this.unsubscribeElement(element);

            if (index == this.delegateWithNulls.size() - 1) {
                ItemEntity remove = this.delegateWithNulls.remove(index);
                if (remove != element) {
                    throw new IllegalStateException("Element mismatch, expected " + element + " but got " + remove);
                }
            } else {
                this.delegateWithNulls.set(index, null); //Set to null so the indices in the category lists stay valid
            }

            this.removeFromCategories(element, index);
        }
        int size = this.delegateWithNulls.size();
        if (size > 64 && size > this.delegate.size() * 2) {
            this.reinitialize();
        }
    }

    /**
     * If the delegate list is significantly larger than the delegateWithNulls list, we compact the delegateWithNulls list
     * to be the same size as the delegate list. This can be very expensive, as all elements need to be unsubscribed and
     * resubscribed, as well as re-categorized.
     */
    private void reinitialize() {
        for (ItemEntity element : this.delegate) {
            this.unsubscribeElement(element);
        }
        this.tempUncategorizedElements.clear();
        this.delegateWithNulls.clear();
        this.elementsByCategory.clear();
        this.maxHalfFullElementsByCategory.clear();

        for (int i = 0, j = 0; i < this.delegate.size(); i++) {
            ItemEntity element = this.delegate.get(i);
            if (!element.getStack().isEmpty()) {
                this.delegateWithNulls.add(element);
                this.addToCategories(element, j, false);
                this.subscribeElement(element, j);
                j++;
            }
        }
    }

    private int unsubscribeElement(ItemEntity element) {
        //noinspection unchecked
        return ((ChangePublisher<ItemEntity>) element).lithium$unsubscribe(this);
    }

    private void removeFromCategories(ItemEntity element, int index) {
        ItemStack stack = element.getStack();
        if (stack.isEmpty()) {
            return;
        }
        removeFromCategoryList(this.elementsByCategory, stack, index);

        if (isMaxHalfFull(stack)) {
            removeFromCategoryList(this.maxHalfFullElementsByCategory, stack, index);
        }
    }

    private static void removeFromCategoryList(Object2ReferenceOpenCustomHashMap<ItemStack, IntArrayList> elementsByCategory, ItemStack stack, int index) {
        IntArrayList categoryList = elementsByCategory.get(stack);
        if (categoryList != null) {
            categoryList.rem(index);
            if (categoryList.isEmpty()) {
                elementsByCategory.remove(stack);
            }
        }
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return this.delegate.containsAll(c);
    }

    @Override
    public void clear() {
        for (ItemEntity element : this.delegate) {
            this.unsubscribeElement(element);
        }
        this.delegate.clear();
        
        this.tempUncategorizedElements.clear();
        this.delegateWithNulls.clear();
        this.elementsByCategory.clear();
        this.maxHalfFullElementsByCategory.clear();
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object o) {
        return this.delegate.equals(o);
    }

    @Override
    public int hashCode() {
        return this.delegate.hashCode();
    }

    @Override
    public ItemEntity get(int index) {
        return this.delegate.get(index);
    }

    @Override
    public ItemEntity set(int i, ItemEntity newElement) {
        ItemEntity previousElement = this.delegate.set(i, newElement);
        if (previousElement != newElement) {
            this.processOutdated();

            int index;
            if (previousElement.getStack().isEmpty()) {
                if (this.delegateWithNulls.size() == this.delegate.size()) {
                    index = i;
                } else {
                    //Have to reinitialize because we cannot reconstruct where to insert
                    this.reinitialize(); //Find a better solution if this shows up in the profiler
                    return previousElement;
                }
            } else {
                index = this.unsubscribeElement(previousElement);
                this.removeFromCategories(previousElement, index);
            }

            if (newElement.getStack().isEmpty()) {
                this.delegateWithNulls.set(index, null);
                return previousElement;
            } else {
                ItemEntity replaced = this.delegateWithNulls.set(index, newElement);
                this.addToCategories(newElement, index, true);
                this.subscribeElement(newElement, index);

                if (replaced != null && replaced != previousElement && !replaced.getStack().isEmpty()) {
                    throw new IllegalStateException("Element mismatch, expected " + previousElement + " but got " + replaced);
                }
            }

        }
        return previousElement;
    }

    @Override
    public int indexOf(Object o) {
        return this.delegate.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return this.delegate.lastIndexOf(o);
    }

    @Override
    public <U> U[] toArray(IntFunction<U[]> generator) {
        return this.delegate.toArray(generator);
    }

    @Override
    public Stream<ItemEntity> stream() {
        return this.delegate.stream();
    }

    @Override
    public Stream<ItemEntity> parallelStream() {
        return this.delegate.parallelStream();
    }

    @Override
    public void forEach(Consumer<? super ItemEntity> action) {
        this.delegate.forEach(action);
    }

    @Override
    public void lithium$notify(@Nullable ItemEntity publisher, int subscriberData) {
        this.markElementAsOutdated(publisher, subscriberData);
    }

    @Override
    public void lithium$forceUnsubscribe(ItemEntity publisher, int subscriberData) {
        this.markElementAsOutdated(publisher, subscriberData);
    }

    @Override
    public void lithium$notifyCount(ItemEntity element, int index, int newCount) {
        this.processOutdated();

        ItemStack stack = element.getStack();
        if (newCount <= 0) {
            this.removeFromCategories(element, index);
            return;
        }

        boolean wasMaxHalfFull = isMaxHalfFull(stack);
        boolean isMaxHalfFull = isMaxHalfFull(newCount, stack.getMaxCount());

        if (wasMaxHalfFull != isMaxHalfFull) {
            if (isMaxHalfFull) {
                addToCategoryList(index, stack, null, this.maxHalfFullElementsByCategory, true);
            } else {
                removeFromCategoryList(this.maxHalfFullElementsByCategory, stack, index);
            }
        }
    }

    public LazyIterationConsumer.NextIteration consumeForEntityStacking(ItemEntity searchingEntity, LazyIterationConsumer<ItemEntity> itemEntityConsumer) {
        this.processOutdated();

        ItemStack stack = searchingEntity.getStack();
        int count = stack.getCount();
        int maxCount = stack.getMaxCount();
        if (count * 2 >= maxCount) { //>=50% full
            // Consume entities that are <= 50% full.
            return this.consumeElements(itemEntityConsumer, this.maxHalfFullElementsByCategory.get(stack));
        } else {
            // Consume entities. We could skip 100% full entities, but we don't group them like this, because this
            // branch is expected to be uncommon, since items usually stack until they are >=50% full.
            return this.consumeElements(itemEntityConsumer, this.elementsByCategory.get(stack));
        }
    }

    private LazyIterationConsumer.NextIteration consumeElements(LazyIterationConsumer<ItemEntity> elementConsumer, IntArrayList categoryList) {
        if (categoryList == null) {
            return LazyIterationConsumer.NextIteration.CONTINUE;
        }
        int expectedModCount = this.modCount;
        int size = categoryList.size();
        for (int i = 0; i < size; i++) {
            if (expectedModCount != this.modCount) {
                throw new ConcurrentModificationException("Collection was modified during iteration!");
            }

            ItemEntity element = this.delegateWithNulls.get(categoryList.getInt(i));

            //The consumer must not modify the consumed element and or other elements in the collection, or must return ABORT.
            LazyIterationConsumer.NextIteration next = elementConsumer.accept(element);
            if (next != LazyIterationConsumer.NextIteration.CONTINUE) {
                return next;
            }
        }
        return LazyIterationConsumer.NextIteration.CONTINUE;
    }
}
