package me.jellysquid.mods.lithium.common.hopper;

import me.jellysquid.mods.lithium.mixin.block.hopper.DefaultedListAccessor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;

public class LithiumStackList extends DefaultedList<ItemStack> {
    LithiumDoubleStackList parent;

    protected int cachedSignalStrength;
    private ComparatorUpdatePattern cachedComparatorUpdatePattern;

    private long modCount;
    final int maxCountPerStack;

    private boolean signalStrengthOverride;

    public LithiumStackList(DefaultedList<ItemStack> original, int maxCountPerStack) {
        //noinspection unchecked
        super(((DefaultedListAccessor<ItemStack>)original).getDelegate(), ItemStack.EMPTY);
        this.maxCountPerStack = maxCountPerStack;

        this.cachedSignalStrength = -1;
        this.cachedComparatorUpdatePattern = null;
        this.modCount = 0;
        this.signalStrengthOverride = false;

        int size = this.size();
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < size; i++) {
            ItemStack stack = this.get(i);
            if (!stack.isEmpty()) {
                //noinspection ConstantConditions
                ((StorableItemStack) (Object) stack).registerToInventory(this);
            }
        }
    }

    public LithiumStackList(int maxCountPerStack) {
        super(null, ItemStack.EMPTY);
        this.maxCountPerStack = maxCountPerStack;
        this.cachedSignalStrength = -1;
    }
    public long getModCount() {
        return this.modCount;
    }

    public void changed() {
        this.cachedSignalStrength = -1;
        this.cachedComparatorUpdatePattern = null;
        this.modCount++;
    }

    @Override
    public ItemStack set(int index, ItemStack element) {
        this.changed();
        ItemStack previous = super.set(index, element);
        if (previous != element) {
            //noinspection ConstantConditions
            ((StorableItemStack) (Object) previous).unregisterFromInventory(this);
            //noinspection ConstantConditions
            ((StorableItemStack) (Object) element).registerToInventory(this);
        }
        return previous;
    }

    @Override
    public void add(int value, ItemStack element) {
        this.changed();
        super.add(value, element);
        //noinspection ConstantConditions
        ((StorableItemStack) (Object) element).registerToInventory(this);
    }

    @Override
    public ItemStack remove(int index) {
        this.changed();
        ItemStack previous = super.remove(index);
        //noinspection ConstantConditions
        ((StorableItemStack) (Object) previous).unregisterFromInventory(this);
        return previous;
    }

    @Override
    public void clear() {
        this.changed();
        int size = this.size();
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < size; i++) {
            ItemStack stack = this.get(i);
            if (!stack.isEmpty()) {
                //noinspection ConstantConditions
                ((StorableItemStack) (Object) stack).unregisterFromInventory(this);
            }
        }
        super.clear();
    }

    public boolean hasSignalStrengthOverride() {
        return this.signalStrengthOverride;
    }

    public int getSignalStrength() {
        if (this.signalStrengthOverride) {
            return 0;
        }
        int signalStrength = this.cachedSignalStrength;
        if (signalStrength == -1) {
            return this.cachedSignalStrength = this.calculateSignalStrength();
        }
        return signalStrength;
    }

    /**
     * [VanillaCopy] {@link net.minecraft.screen.ScreenHandler#calculateComparatorOutput(Inventory)}
     * @return the signal strength for this inventory
     */
    int calculateSignalStrength() {
        int i = 0;
        float f = 0.0F;

        //noinspection ForLoopReplaceableByForEach
        for(int j = 0; j < this.size(); ++j) {
            ItemStack itemStack = this.get(j);
            if (!itemStack.isEmpty()) {
                f += (float)itemStack.getCount() / (float)Math.min(this.maxCountPerStack, itemStack.getMaxCount());
                ++i;
            }
        }

        f /= (float)this.size();
        return MathHelper.floor(f * 14.0F) + (i > 0 ? 1 : 0);
    }

    public void setReducedSignalStrengthOverride() {
        this.signalStrengthOverride = true;
    }

    public void clearSignalStrengthOverride() {
        this.signalStrengthOverride = false;
    }


    /**
     * @param masterStackList the stacklist of the inventory that comparators read from (double inventory for double chests)
     * @param inventory the blockentity / inventory that this stacklist is inside
     */
    public void runComparatorUpdatePatternOnFailedExtract(LithiumStackList masterStackList, Inventory inventory) {
        if (inventory instanceof BlockEntity) {
            if (this.cachedComparatorUpdatePattern == null) {
                this.cachedComparatorUpdatePattern = HopperHelper.determineComparatorUpdatePattern(inventory, masterStackList);
            }
            this.cachedComparatorUpdatePattern.apply((BlockEntity) inventory, masterStackList);
        }
    }
}
