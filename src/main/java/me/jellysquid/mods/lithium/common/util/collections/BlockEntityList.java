package me.jellysquid.mods.lithium.common.util.collections;

import it.unimi.dsi.fastutil.longs.Long2ReferenceLinkedOpenHashMap;
import net.minecraft.block.entity.BlockEntity;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class BlockEntityList implements List<BlockEntity> {
    private final Long2ReferenceLinkedOpenHashMap<BlockEntity> map = new Long2ReferenceLinkedOpenHashMap<>();

    public BlockEntityList(List<BlockEntity> list) {
        this.addAll(list);
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof BlockEntity) {
            return this.map.containsKey(getEntityPos((BlockEntity) o));
        }

        return false;
    }

    @Override
    public Iterator<BlockEntity> iterator() {
        return this.map.values().iterator();
    }

    @Override
    public Object[] toArray() {
        return this.map.values().toArray();
    }

    @Override
    @SuppressWarnings("SuspiciousToArrayCall")
    public <T> T[] toArray(T[] a) {
        return this.map.values().toArray(a);
    }

    @Override
    public boolean add(BlockEntity blockEntity) {
        long pos = getEntityPos(blockEntity);

        BlockEntity prev = this.map.putAndMoveToLast(pos, blockEntity);

        // Replacing a block entity should always mark the previous entry as removed
        if (prev != null && prev != blockEntity) {
            prev.markRemoved();
        }

        return true;
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof BlockEntity) {
            BlockEntity blockEntity = (BlockEntity) o;

            return this.map.remove(getEntityPos(blockEntity), blockEntity);
        }

        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object obj : c) {
            if (!(obj instanceof BlockEntity)) {
                return false;
            } else if (this.map.get(getEntityPos((BlockEntity) obj)) == obj) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean addAll(Collection<? extends BlockEntity> c) {
        for (BlockEntity blockEntity : c) {
            this.add(blockEntity);
        }

        return true;
    }

    @Override
    public boolean addAll(int index, Collection<? extends BlockEntity> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean modified = false;

        for (Object obj : c) {
            if (obj instanceof BlockEntity) {
                BlockEntity blockEntity = (BlockEntity) obj;

                if (this.map.remove(getEntityPos(blockEntity), blockEntity)) {
                    modified = true;
                }
            }
        }

        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return this.map.values()
                .retainAll(c);
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    @Override
    public BlockEntity get(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BlockEntity set(int index, BlockEntity element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, BlockEntity element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BlockEntity remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<BlockEntity> listIterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<BlockEntity> listIterator(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<BlockEntity> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    private static long getEntityPos(BlockEntity e) {
        return e.getPos().asLong();
    }

    public BlockEntity getEntityAtPosition(long pos) {
        return this.map.get(pos);
    }

    public boolean tryAdd(BlockEntity entity) {
        return this.map.putIfAbsent(getEntityPos(entity), entity) == null;
    }
}
