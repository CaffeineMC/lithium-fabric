package me.jellysquid.mods.lithium.common.entity.item;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import me.jellysquid.mods.lithium.mixin.util.accessors.ItemStackAccessor;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class ItemEntityCategorizingList extends AbstractList<ItemEntity> {
    public static final int ITEM_ENTITY_CATEGORIZATION_THRESHOLD = 20;

    private final ArrayList<ItemEntity> delegate;
    private Reference2ReferenceOpenHashMap<Item, ArrayList<ItemEntity>> itemEntitiesByItem;

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

        ArrayList<ItemEntity> itemEntities = this.itemEntitiesByItem.get(((ItemStackAccessor) (Object) searchingEntity.getStack()).lithium$getItem());
        if (itemEntities == null) {
            return Collections.emptyList();
        }
        return itemEntities;
    }


    public void invalidateCache() {
        this.itemEntitiesByItem = null;
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

            ArrayList<ItemEntity> itemEntities = this.itemEntitiesByItem.get(category);
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
            ArrayList<ItemEntity> itemEntities = this.itemEntitiesByItem.get(category);
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
}
