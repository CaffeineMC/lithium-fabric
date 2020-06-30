package me.jellysquid.mods.lithium.common.world;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class NullBlockEntityList<E extends BlockEntity> implements List<E> {
    @Override
    public int size() {
        throw new UnsupportedOperationException("Lithium does not support this");
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public Iterator<E> iterator() {
        throw new UnsupportedOperationException("Lithium does not support this");
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException("Lithium does not support this");
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException("Lithium does not support this");
    }

    /**
     * Since {@link World#addBlockEntity(BlockEntity)} uses the response from {@link World#blockEntities#add(BlockEntity)},
     * which we "deleted", we need to always return {@code true}.
     *
     * @author Maity
     */
    @Override
    public boolean add(E e) {
        return true;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return false;
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {}

    @Override
    public E get(int index) {
        throw new UnsupportedOperationException("Lithium does not support this");
    }

    @Override
    public E set(int index, E element) {
        throw new UnsupportedOperationException("Lithium does not support this");
    }

    @Override
    public void add(int index, E element) {}

    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException("Lithium does not support this");
    }

    @Override
    public int indexOf(Object o) {
        throw new UnsupportedOperationException("Lithium does not support this");
    }

    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException("Lithium does not support this");
    }

    @Override
    public ListIterator<E> listIterator() {
        throw new UnsupportedOperationException("Lithium does not support this");
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        throw new UnsupportedOperationException("Lithium does not support this");
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("Lithium does not support this");
    }
}
