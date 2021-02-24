package me.jellysquid.mods.lithium.common.util.collections;

import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

public class MaskedTickingBlockEntityList<T> implements List<T> {
    private final Predicate<T> mayContain;

    private final Reference2IntOpenHashMap<T> allElements2Index;
    private final ReferenceArrayList<T> allElements;

    private final IntArrayList filteredSuccessor;
    private final BitSet filteredElementsMask;

    private int firstRemovedIndex;

    //Visualization of the internal datastructures
    //indices:      0 1 2 3 4 5 6 7 8 9
    //allElements:  A B C D - F G H I J  //E was fully removed, C,F,G,I were filtered away
    //filteredMask: 1 1 0 1 0 0 0 1 0 1
    //successor:  0 1 3 - 7 - - - 9 - - (index offset by 1, due to the first successor having to point to the first element)
    //Removals from the allElements ArrayList are done by setting the value to null
    //The successor list is used to iterate through the allElements ArrayList with an increasing index, but skipping long chains of null elements.
    //The BitSet mask is used to find the predecessor and successor quickly (not asymptotically fast, but fast enough)

    public MaskedTickingBlockEntityList(List<T> list, Predicate<T> mayContain) {
        this.mayContain = mayContain;
        this.allElements = new ReferenceArrayList<>();
        this.allElements2Index = new Reference2IntOpenHashMap<>();
        this.allElements2Index.defaultReturnValue(-1);
        this.filteredSuccessor = new IntArrayList();
        this.filteredElementsMask = new BitSet();
        this.firstRemovedIndex = Integer.MAX_VALUE;

        for (T t : list) {
            if (this.mayContain.test(t)) {
                int index = this.allElements.size();
                this.allElements.add(t);
                this.filteredElementsMask.set(index);
                this.filteredSuccessor.add(index);
                this.allElements2Index.put(t, index);
            }
        }
        this.filteredSuccessor.add(-1);
    }

    public void setEntryVisible(T t, boolean value) {
        this.setEntryVisible(this.allElements2Index.getOrDefault(t, -1), value);
    }

    public void setEntryVisible(int index, boolean value) {
        //Visualization of the operations possible
        //All:         A B C D - F G H I J (- for null)
        //filteredMask:1 1 0 1 0 0 0 1 0 1
        //indices:     0 1 2 3 4 5 6 7 8 9
        //successor: 0 1 3 - 7 - - - 9 - - (- for no successor)
        //Set F visible:
        //All:         A B C D - F G H I J
        //filteredMask:1 1 0 1 0 1 0 1 0 1  //set mask at F to 1
        //indices:     0 1 2 3 4 5 6 7 8 9
        //successor: 0 1 3 - 5 - 7 - 9 - -  //update successor of predecessor to F and set F's successor to old successor of predecessor
        //Set D filtered:
        //All:         A B C D - F G H I J
        //Mask:        1 1 0 0 0 1 0 1 0 1  //set mask at D to 0
        //indices:     0 1 2 3 4 5 6 7 8 9
        //successor: 0 1 5 - - - 7 - 9 - -  //update successor of predecessor to old successor of D and remove D's successor value

        //These calls do not modify the size, they can't cause rehashing, they are safe to use during iteration
        if (index == -1 || value == this.filteredElementsMask.get(index)) {
            return;
        }

        this.filteredElementsMask.set(index, value);
        int predecessor = this.filteredElementsMask.previousSetBit(index - 1);
        if (value) {
            int successor = this.filteredSuccessor.getInt(predecessor + 1);
            this.filteredSuccessor.set(predecessor + 1, index);
            this.filteredSuccessor.set(index + 1, successor);
        } else {
            int successor = this.filteredSuccessor.getInt(index + 1);
            this.filteredSuccessor.set(predecessor + 1, successor);
            this.filteredSuccessor.set(index + 1, -2); //no successor as this element cannot be reached
        }
    }

    private void compact() {
        int targetSize = this.size();
        int newIndex = this.firstRemovedIndex - 1;
        int lastVisible = this.filteredElementsMask.previousSetBit(newIndex);


        for (int i = newIndex + 1; i < this.allElements.size(); i++) {
            T t = this.allElements.get(i);
            if (t == null) {
                continue;
            }
            boolean visible = this.filteredElementsMask.get(i);
            //shift all entries to the lower indices (filling the gaps created by remove() setting null)
            newIndex++;
            //i is guaranteed to not be smaller than newIndex, therefore we can write to the same collections

            this.allElements.set(newIndex, t);
            this.allElements2Index.put(t, newIndex);
            this.filteredElementsMask.set(newIndex, visible);

            //update the successor links
            this.filteredSuccessor.set(newIndex + 1, -2); //no successor as there is no next entry yet
            if (visible) {
                this.filteredSuccessor.set(lastVisible + 1, newIndex);
                lastVisible = newIndex;
            }
        }

        if (newIndex + 1 != targetSize) {
            throw new IllegalStateException("Compaction ended up with incorrect size: Should be: " + targetSize + " but is: " + (newIndex + 1));
        }

        this.filteredSuccessor.set(lastVisible + 1, -1); //-1 means this was the last element
        this.firstRemovedIndex = Integer.MAX_VALUE;

        this.filteredSuccessor.removeElements(targetSize + 1, this.filteredSuccessor.size());
        this.allElements.removeElements(targetSize, this.allElements.size());
        this.filteredElementsMask.clear(targetSize, this.filteredElementsMask.size());

        this.filteredSuccessor.trim(targetSize * 4);
        this.allElements.trim(targetSize * 4);
        this.allElements2Index.trim(targetSize * 4);
    }

    public Iterator<T> filteredIterator() {
        return new Iterator<T>() {
            int next = MaskedTickingBlockEntityList.this.filteredSuccessor.getInt(0);
            T prev;

            @Override
            public boolean hasNext() {
                return this.next != -1;
            }

            @Override
            public T next() {
                int next = this.next;
                T prev = MaskedTickingBlockEntityList.this.allElements.get(next);
                this.prev = prev;
                this.next = MaskedTickingBlockEntityList.this.filteredSuccessor.getInt(next + 1);
                return prev;
            }

            @Override
            public void remove() {
                MaskedTickingBlockEntityList.this.remove(this.prev);
            }
        };
    }

    @Override
    public int size() {
        return this.allElements2Index.size();
    }

    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        //noinspection SuspiciousMethodCalls
        return this.allElements2Index.containsKey(o);
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return Iterators.unmodifiableIterator(this.allElements2Index.keySet().iterator());
    }

    @Override
    public Object[] toArray() {
        return this.allElements2Index.keySet().toArray();
    }

    @NotNull
    @Override
    public <T1> T1[] toArray(@NotNull T1[] t1s) {
        //noinspection SuspiciousToArrayCall
        return this.allElements2Index.keySet().toArray(t1s);
    }

    @Override
    public boolean add(T t) {
        int arraySize = this.allElements.size();
        int invalidEntries = arraySize - this.size();
        //Compaction is done during the add operation as it is guaranteed to not happen during iteration
        if ((arraySize > 2048 && invalidEntries > (arraySize >> 1)) || arraySize >= Integer.MAX_VALUE - 1 && invalidEntries != 0) {
            this.compact();
        }

        if (!this.mayContain.test(t)) {
            return false;
        }

        int index = this.allElements.size();
        int i = this.allElements2Index.putIfAbsent(t, index);
        if (i != -1) {
            return false;
        }
        this.allElements.add(t);
        this.filteredSuccessor.add(0);//increase size so setEntryVisible doesn't crash with indexOutOfBounds
        this.setEntryVisible(index, true);

        return true;
    }

    @Override
    public boolean remove(Object o) {
        int index = this.allElements2Index.removeInt(o);
        if (index == -1) {
            return false;
        }
        this.setEntryVisible(index, false);
        this.allElements.set(index, null);
        this.firstRemovedIndex = Math.min(this.firstRemovedIndex, index);
        return true;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return this.allElements2Index.keySet().containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        boolean b = false;
        for (T t : c) {
            this.add(t);
            b = true;
        }
        return b;
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        boolean b = false;
        for (Object t : c) {
            b |= this.remove(t);
        }
        return b;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        this.allElements2Index.clear();
        this.allElements.clear();
        this.filteredSuccessor.clear();
        this.filteredElementsMask.clear();
        this.firstRemovedIndex = Integer.MAX_VALUE;
        this.filteredSuccessor.add(-1);
    }

    @Override
    public T get(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T set(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T remove(int index) {
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
    public @NotNull ListIterator<T> listIterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull ListIterator<T> listIterator(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull List<T> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }
}
