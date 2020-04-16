package me.jellysquid.mods.lithium.common.util;


import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;

import java.util.*;
import java.util.function.Predicate;

/**
 * Class for lazy evaluation of collision box checks and predicates over chunk section entity iterators.
 * Use this when entities are used greedily and not the whole list is consumed. Also make sure that the list won't
 * be queried further after the chunk section entity lists can be modified. (throws ConcurrentModificationException)
 * Supposed to act like an ArrayList wrapped in UnmodifiableCollection
 * However if editing the list is required in the future, those methods can be implemented.
 */
public class EntityIteratorsAsList implements List<Entity> {
    private final ArrayList<Iterator<Entity>> iterators = new ArrayList<>();
    private int iteratorIndex;
    private final ArrayList<Entity> elements = new ArrayList<>();
    private final ReferenceOpenHashSet<Entity> elementsUnsorted = new ReferenceOpenHashSet<>();
    private int nextElementIndex;

    private final Entity excluded;
    private final Box entityBox;
    private final Predicate<Entity> entityPredicate;

    public EntityIteratorsAsList(Entity excluded, Box box, Predicate<Entity> entityPredicate) {
        this.excluded = excluded;
        this.entityBox = box;
        this.entityPredicate = entityPredicate;
    }

    public void appendIterator(Iterator<Entity> iterator) {
        this.iterators.add(iterator);
    }

    private void collectUntilIndex(int end) {
        Iterator<Entity> it = this.iterators.get(this.iteratorIndex);
        while(this.nextElementIndex <= end) {
            while (!it.hasNext()) {
                this.iteratorIndex++;
                if (this.iteratorIndex >= this.iterators.size()) {
                    return;
                };
                it = this.iterators.get(this.iteratorIndex);
            }
            Entity entity = it.next();
            if (this.excluded != entity && (this.entityBox == null || this.entityBox.intersects(entity.getBoundingBox())) &&
                    (this.entityPredicate == null || this.entityPredicate.test(entity))) {
                this.elements.add(entity);
                this.elementsUnsorted.add(entity);
                ++this.nextElementIndex;
            }
        }

    }

    @Override
    public int size() {
        this.collectUntilIndex(Integer.MAX_VALUE);
        return this.nextElementIndex;
    }

    @Override
    public boolean isEmpty() {
        this.collectUntilIndex(0);
        return this.nextElementIndex == 0;
    }

    @Override
    public boolean contains(Object o) {
        if (!(o instanceof Entity) || o == this.excluded) {
            return false;
        }
        if (this.elementsUnsorted.contains(o)) {
            return true;
        }

        Iterator<Entity> it = this.iterators.get(this.iteratorIndex);
        while(true) {
            while (!it.hasNext()) {
                this.iteratorIndex++;
                if (this.iteratorIndex >= this.iterators.size()) {
                    return false;
                };
                it = this.iterators.get(this.iteratorIndex);
            }
            Entity entity = it.next();
            if (this.excluded != entity && (this.entityBox == null || this.entityBox.intersects(entity.getBoundingBox())) &&
                    (this.entityPredicate == null || this.entityPredicate.test(entity))) {
                this.elements.add(entity);
                this.elementsUnsorted.add(entity);
                ++this.nextElementIndex;
                if (entity == o) {
                    return true;
                }
            }

        }
    }

    @Override
    public Iterator<Entity> iterator() {
        return new Iterator<Entity>() {
            int nextIndex;
            @Override
            public boolean hasNext() {
                if (EntityIteratorsAsList.this.nextElementIndex <= this.nextIndex) {
                    EntityIteratorsAsList.this.collectUntilIndex(this.nextIndex);
                }
                return this.nextIndex < EntityIteratorsAsList.this.nextElementIndex;
            }

            @Override
            public Entity next() {
                return EntityIteratorsAsList.this.elements.get(this.nextIndex++);
            }
        };
    }

    @Override
    public Object[] toArray() {
        this.collectUntilIndex(Integer.MAX_VALUE);
        return this.elements.toArray();
    }

    @Override
    public <T> T[] toArray(T[] ts) {
        this.collectUntilIndex(Integer.MAX_VALUE);
        return this.elements.toArray(ts);
    }


    @Override
    public boolean add(Entity entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return this.elementsUnsorted.containsAll(collection);
    }

    @Override
    public boolean addAll(Collection<? extends Entity> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int i, Collection<? extends Entity> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Entity get(int i) {
        this.collectUntilIndex(i);
        return this.elements.get(i);
    }

    @Override
    public Entity set(int i, Entity entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int i, Entity entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Entity remove(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
        return this.elements.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return this.elements.lastIndexOf(o);
    }

    @Override
    public ListIterator<Entity> listIterator() {
        //naive implementation, likely unused
        this.collectUntilIndex(Integer.MAX_VALUE);
        return this.elements.listIterator();
    }

    @Override
    public ListIterator<Entity> listIterator(int i) {
        //naive implementation, likely unused
        this.collectUntilIndex(Integer.MAX_VALUE);
        return this.elements.listIterator(i);
    }

    @Override
    public List<Entity> subList(int i, int i1) {
        //naive implementation, likely unused
        this.collectUntilIndex(i1);
        return this.elements.subList(i, i1);
    }
}
