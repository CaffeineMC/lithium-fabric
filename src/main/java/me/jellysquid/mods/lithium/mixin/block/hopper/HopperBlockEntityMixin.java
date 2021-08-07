package me.jellysquid.mods.lithium.mixin.block.hopper;

import me.jellysquid.mods.lithium.api.inventory.LithiumInventory;
import me.jellysquid.mods.lithium.common.entity.tracker.nearby.SectionedInventoryEntityMovementTracker;
import me.jellysquid.mods.lithium.common.entity.tracker.nearby.SectionedItemEntityMovementTracker;
import me.jellysquid.mods.lithium.common.hopper.HopperHelper;
import me.jellysquid.mods.lithium.common.hopper.InventoryHelper;
import me.jellysquid.mods.lithium.common.hopper.LithiumStackList;
import me.jellysquid.mods.lithium.common.hopper.UpdateReceiver;
import net.minecraft.block.BlockState;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.minecraft.block.entity.HopperBlockEntity.getInputItemEntities;


@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin extends BlockEntity implements Hopper, UpdateReceiver, LithiumInventory {
    private static final Inventory USE_ENTITY_INVENTORY = new SimpleInventory(0);

    public HopperBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Shadow
    @Nullable
    private static native Inventory getInputInventory(World world, Hopper hopper);

    @Shadow
    private static native boolean insert(World world, BlockPos pos, BlockState state, Inventory inventory);

    @Shadow
    protected abstract boolean isDisabled();

    @Shadow
    private long lastTickTime;

    @Shadow
    protected abstract void setCooldown(int cooldown);

    @Shadow
    private static native boolean canExtract(Inventory inv, ItemStack stack, int slot, Direction facing);

    private long myLastInsertChangeCount, myLastExtractChangeCount, myLastCollectChangeCount;

    //these fields, together with the removedCount are storing the relevant data for deciding whether a cached inventory can be used again
    //does not store the block entities for cache invalidation reasons
    //null means unknown or inventory blockentity present (use the LithiumInventory directly), USE_ENTITY_INVENTORY means no blockentity and no composter, composter inventory means composter present
    private Inventory insertBlockInventory, extractBlockInventory;
    //any optimized inventories interacted with are stored (including entities) with extra data
    private LithiumInventory insertInventory, extractInventory;
    private int insertInventoryRemovedCount, extractInventoryRemovedCount;
    private LithiumStackList insertInventoryStackList, extractInventoryStackList;
    private long insertInventoryChangeCount, extractInventoryChangeCount;

    private SectionedItemEntityMovementTracker<ItemEntity> extractItemEntityTracker;
    private boolean extractItemEntityTrackerWasEmpty;
    //item pickup bounding boxes in order. The last box in the array is the box that encompasses all of the others
    private Box[] extractItemEntityBoxes;
    private long extractItemEntityAttemptTime;
    private SectionedInventoryEntityMovementTracker<Inventory> extractInventoryEntityTracker;
    private Box extractInventoryEntityBox;
    private long extractInventoryEntityAttemptTime;
    private SectionedInventoryEntityMovementTracker<Inventory> insertInventoryEntityTracker;
    private Box insertInventoryEntityBox;
    private long insertInventoryEntityAttemptTime;

    @Redirect(method = "extract(Lnet/minecraft/world/World;Lnet/minecraft/block/entity/Hopper;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/HopperBlockEntity;getInputInventory(Lnet/minecraft/world/World;Lnet/minecraft/block/entity/Hopper;)Lnet/minecraft/inventory/Inventory;"))
    private static Inventory getExtractInventory(World world, Hopper hopper) {
        if (!(hopper instanceof HopperBlockEntityMixin hopperBlockEntity)) {
            return getInputInventory(world, hopper); //Hopper Minecarts do not cache Inventories
        }

        Inventory blockInventory = hopperBlockEntity.getExtractBlockInventory(world);
        if (blockInventory != null) {
            return blockInventory;
        }

        if (hopperBlockEntity.extractInventoryEntityTracker == null) {
            hopperBlockEntity.initExtractInventoryTracker(world);
        }
        if (hopperBlockEntity.extractInventoryEntityTracker.isUnchangedSince(hopperBlockEntity.extractInventoryEntityAttemptTime)) {
            return null;
        }
        hopperBlockEntity.extractInventoryEntityAttemptTime = Long.MIN_VALUE;

        hopperBlockEntity.myLastCollectChangeCount = InventoryHelper.getLithiumStackList(hopperBlockEntity).getModCount();

        List<Inventory> inventoryEntities = hopperBlockEntity.extractInventoryEntityTracker.getEntities(hopperBlockEntity.extractInventoryEntityBox);
        if (inventoryEntities.isEmpty()) {
            hopperBlockEntity.extractInventoryEntityAttemptTime = hopperBlockEntity.lastTickTime;
            //only set unchanged when no entity present. this allows shortcutting this case
            //shortcutting the entity present case requires checking its change counter
            return null;
        }
        Inventory inventory = inventoryEntities.get(world.random.nextInt(inventoryEntities.size()));
        if (inventory != hopperBlockEntity.extractInventory && inventory instanceof LithiumInventory optimizedInventory) {
            //not caching the inventory (hopperBlockEntity.extractBlockInventory == USE_ENTITY_INVENTORY prevents it)
            //make change counting on the entity inventory possible, without caching it as block inventory
            hopperBlockEntity.extractInventory = optimizedInventory;
            hopperBlockEntity.extractInventoryStackList = InventoryHelper.getLithiumStackList(optimizedInventory);
            hopperBlockEntity.extractInventoryChangeCount = hopperBlockEntity.extractInventoryStackList.getModCount() - 1;
        }
        return inventory;
    }

    /**
     * Inject to replace the extract method with an optimized but equivalent replacement.
     * Uses the vanilla method as fallback for non-optimized Inventories.
     *
     * @param to   Hopper or Hopper Minecart that is extracting
     * @param from Inventory the hopper is extracting from
     */
    @Inject(method = "extract(Lnet/minecraft/world/World;Lnet/minecraft/block/entity/Hopper;)Z", at = @At(value = "FIELD", target = "Lnet/minecraft/util/math/Direction;DOWN:Lnet/minecraft/util/math/Direction;", shift = At.Shift.AFTER), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private static void lithiumExtract(World world, Hopper to, CallbackInfoReturnable<Boolean> cir, Inventory from) {
        if (!(to instanceof HopperBlockEntityMixin hopperBlockEntity)) {
            return; //optimizations not implemented for hopper minecarts
        }
        if (from != hopperBlockEntity.extractInventory) {
            return; //from inventory is not an optimized inventory, vanilla fallback
        }

        LithiumStackList hopperStackList = InventoryHelper.getLithiumStackList(hopperBlockEntity);
        LithiumStackList fromStackList = hopperBlockEntity.extractInventoryStackList;

        if (hopperStackList.getModCount() == hopperBlockEntity.myLastExtractChangeCount) {
            if (fromStackList.getModCount() == hopperBlockEntity.extractInventoryChangeCount) {
                //noinspection CollectionAddedToSelf
                fromStackList.runComparatorUpdatePatternOnFailedExtract(fromStackList, from);
                cir.setReturnValue(false);
                return;
            }
        }

        int[] availableSlots = from instanceof SidedInventory ? ((SidedInventory) from).getAvailableSlots(Direction.DOWN) : null;
        int fromSize = availableSlots != null ? availableSlots.length : from.size();
        for (int i = 0; i < fromSize; i++) {
            int fromSlot = availableSlots != null ? availableSlots[i] : i;
            ItemStack itemStack = fromStackList.get(fromSlot);
            if (!itemStack.isEmpty() && canExtract(from, itemStack, fromSlot, Direction.DOWN)) {
                //calling removeStack is necessary due to its side effects (markDirty in LootableContainerBlockEntity)
                ItemStack takenItem = from.removeStack(fromSlot, 1);
                assert !takenItem.isEmpty();
                boolean transferSuccess = HopperHelper.tryMoveSingleItem(to, takenItem, null);
                if (transferSuccess) {
                    to.markDirty();
                    from.markDirty();
                    cir.setReturnValue(true);
                    return;
                }
                //put the item back similar to vanilla
                ItemStack restoredStack = fromStackList.get(fromSlot);
                if (restoredStack.isEmpty()) {
                    restoredStack = takenItem;
                } else {
                    restoredStack.increment(1);
                }
                //calling setStack is necessary due to its side effects (markDirty in LootableContainerBlockEntity)
                from.setStack(fromSlot, restoredStack);
            }
        }
        hopperBlockEntity.myLastExtractChangeCount = hopperStackList.getModCount();
        if (fromStackList != null) {
            hopperBlockEntity.extractInventoryChangeCount = fromStackList.getModCount();
        }
        cir.setReturnValue(false);
    }

    @Redirect(
            method = "insertAndExtract(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/entity/HopperBlockEntity;Ljava/util/function/BooleanSupplier;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/entity/HopperBlockEntity;isFull()Z"
            )
    )
    private static boolean lithiumHopperIsFull(HopperBlockEntity hopperBlockEntity) {
        //noinspection ConstantConditions
        LithiumStackList lithiumStackList = InventoryHelper.getLithiumStackList((HopperBlockEntityMixin) (Object) hopperBlockEntity);
        return lithiumStackList.getFullSlots() == lithiumStackList.size();
    }

    @Redirect(
            method = "insertAndExtract(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/entity/HopperBlockEntity;Ljava/util/function/BooleanSupplier;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/entity/HopperBlockEntity;isEmpty()Z"
            )
    )
    private static boolean lithiumHopperIsEmpty(HopperBlockEntity hopperBlockEntity) {
        //noinspection ConstantConditions
        LithiumStackList lithiumStackList = InventoryHelper.getLithiumStackList((HopperBlockEntityMixin) (Object) hopperBlockEntity);
        return lithiumStackList.getOccupiedSlots() == 0;
    }

    /**
     * Effectively overwrites {@link HopperBlockEntity#insert(World, BlockPos, BlockState, Inventory)} (only usage redirect)
     * [VanillaCopy] general hopper insert logic, modified for optimizations
     *
     * @reason Adding the inventory caching into the static method using mixins seems to be unfeasible without temporarily storing state in static fields.
     */
    @SuppressWarnings("JavadocReference")
    @Redirect(method = "insertAndExtract(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/entity/HopperBlockEntity;Ljava/util/function/BooleanSupplier;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/HopperBlockEntity;insert(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/inventory/Inventory;)Z"))
    private static boolean lithiumInsert(World world, BlockPos pos, BlockState hopperState, Inventory hopper) {
        HopperBlockEntityMixin hopperBlockEntity = (HopperBlockEntityMixin) hopper;
        Inventory insertInventory = hopperBlockEntity.getInsertInventory(world, hopperState);
        if (insertInventory == null) {
            //call the vanilla code, but with target inventory nullify (mixin above) to allow other mods inject features
            //e.g. carpet mod allows hoppers to insert items into wool blocks
            return insert(world, pos, hopperState, hopper);
        }

        LithiumStackList hopperStackList = InventoryHelper.getLithiumStackList(hopperBlockEntity);
        if (hopperBlockEntity.insertInventory == insertInventory && hopperStackList.getModCount() == hopperBlockEntity.myLastInsertChangeCount) {
            if (hopperBlockEntity.insertInventoryStackList.getModCount() == hopperBlockEntity.insertInventoryChangeCount) {
//                ComparatorUpdatePattern.NO_UPDATE.apply(hopperBlockEntity, hopperStackList); //commented because it's a noop, Hoppers do not send useless comparator updates
                return false;
            }
        }

        //todo maybe should check whether the receiving inventory is not full first, like vanilla. However this is a rare shortcut case and increases the work most of the time. worst case is 5x work than with the check
        boolean insertInventoryWasEmptyHopperNotDisabled = insertInventory instanceof HopperBlockEntityMixin && !((HopperBlockEntityMixin) insertInventory).isDisabled() && hopperBlockEntity.insertInventoryStackList.getOccupiedSlots() == 0;
        Direction fromDirection = hopperState.get(HopperBlock.FACING).getOpposite();
        int size = hopperStackList.size();
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < size; ++i) {
            ItemStack transferStack = hopperStackList.get(i);
            if (!transferStack.isEmpty()) {
                boolean transferSuccess = HopperHelper.tryMoveSingleItem(insertInventory, transferStack, fromDirection);
                if (transferSuccess) {
                    if (insertInventoryWasEmptyHopperNotDisabled) {
                        HopperBlockEntityMixin receivingHopper = (HopperBlockEntityMixin) insertInventory;
                        int k = 8;
                        if (receivingHopper.lastTickTime >= hopperBlockEntity.lastTickTime) {
                            k = 7;
                        }
                        receivingHopper.setCooldown(k);
                    }
                    insertInventory.markDirty();
                    return true;
                }
            }
        }
        hopperBlockEntity.myLastInsertChangeCount = hopperStackList.getModCount();
        if (hopperBlockEntity.insertInventoryStackList != null) {
            hopperBlockEntity.insertInventoryChangeCount = hopperBlockEntity.insertInventoryStackList.getModCount();
        }
        return false;
    }

    @Override
    public void onNeighborUpdate(boolean above) {
        //Clear the block inventory cache (composter inventories and no inventory present) on block update / observer update
        if (above) {
            if (this.extractBlockInventory != null) {
                //this.extractBlockInventory is either USE_ENTITY_INVENTORY or is a composter. Invalidation on neighbor change required.
                this.invalidateBlockExtractionData();
            }
        } else {
            if (this.insertBlockInventory != null) {
                //this.insertBlockInventory is either USE_ENTITY_INVENTORY or is a composter. Invalidation on neighbor change required.
                this.invalidateBlockInsertionData();
            }
        }
    }


    @Redirect(method = "insert(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/inventory/Inventory;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/HopperBlockEntity;getOutputInventory(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Lnet/minecraft/inventory/Inventory;"))
    private static Inventory nullify(World world, BlockPos pos, BlockState state) {
        return null;
    }

    @Redirect(method = "extract(Lnet/minecraft/world/World;Lnet/minecraft/block/entity/Hopper;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/HopperBlockEntity;getInputItemEntities(Lnet/minecraft/world/World;Lnet/minecraft/block/entity/Hopper;)Ljava/util/List;"))
    private static List<ItemEntity> lithiumGetInputItemEntities(World world, Hopper hopper) {
        if (!(hopper instanceof HopperBlockEntityMixin hopperBlockEntity)) {
            return getInputItemEntities(world, hopper); //optimizations not implemented for hopper minecarts
        }

        if (hopperBlockEntity.extractItemEntityTracker == null) {
            hopperBlockEntity.initExtractItemEntityTracker();
        }
        long modCount = InventoryHelper.getLithiumStackList(hopperBlockEntity).getModCount();
        if ((hopperBlockEntity.extractItemEntityTrackerWasEmpty || hopperBlockEntity.myLastCollectChangeCount == modCount) &&
                hopperBlockEntity.extractItemEntityTracker.isUnchangedSince(hopperBlockEntity.extractItemEntityAttemptTime)) {
            return Collections.emptyList();
        }
        hopperBlockEntity.myLastCollectChangeCount = modCount;

        List<ItemEntity> itemEntities = hopperBlockEntity.extractItemEntityTracker.getEntities(hopperBlockEntity.extractItemEntityBoxes);
        hopperBlockEntity.extractItemEntityAttemptTime = hopperBlockEntity.lastTickTime;
        hopperBlockEntity.extractItemEntityTrackerWasEmpty = itemEntities.isEmpty();
        //set unchanged so that if this extract fails and there is no other change to hoppers or items, extracting
        // items can be skipped.
        return itemEntities;
    }

    /**
     * Makes this hopper remember the given inventory.
     *
     * @param insertInventory Block inventory / Blockentity inventory to be remembered
     */
    private void cacheInsertBlockInventory(Inventory insertInventory) {
        assert !(insertInventory instanceof Entity);
        if (insertInventory instanceof BlockEntity || insertInventory instanceof DoubleInventory) {
            this.insertBlockInventory = null;
        } else {
            this.insertBlockInventory = insertInventory == null ? USE_ENTITY_INVENTORY : insertInventory;
        }

        if (insertInventory instanceof LithiumInventory optimizedInventory) {
            this.insertInventory = optimizedInventory;
            LithiumStackList insertInventoryStackList = InventoryHelper.getLithiumStackList(optimizedInventory);
            this.insertInventoryStackList = insertInventoryStackList;
            this.insertInventoryChangeCount = insertInventoryStackList.getModCount() - 1;
            this.insertInventoryRemovedCount = optimizedInventory.getRemovedCountLithium();
        } else {
            this.insertInventory = null;
            this.insertInventoryStackList = null;
            this.insertInventoryChangeCount = 0;
            this.insertInventoryRemovedCount = 0;
        }
    }

    public Inventory getInsertBlockInventory(World world, BlockState hopperState) {
        Inventory inventory = this.insertBlockInventory;
        if (inventory != null) {
            return inventory == USE_ENTITY_INVENTORY ? null : inventory;
        }
        LithiumInventory optimizedInventory;
        if ((optimizedInventory = this.insertInventory) != null) {
            if (optimizedInventory.getRemovedCountLithium() == this.insertInventoryRemovedCount) {
                return optimizedInventory;
            }
        }
        Direction direction = hopperState.get(HopperBlock.FACING);
        inventory = HopperHelper.vanillaGetBlockInventory(world, this.getPos().offset(direction));
        this.cacheInsertBlockInventory(inventory);
        return inventory;
    }

    /**
     * @author 2No2Name
     * @reason avoid stream code
     */
    @Overwrite
    private static boolean isInventoryEmpty(Inventory inv, Direction side) {
        int[] availableSlots = inv instanceof SidedInventory ? ((SidedInventory) inv).getAvailableSlots(side) : null;
        int fromSize = availableSlots != null ? availableSlots.length : inv.size();
        for (int i = 0; i < fromSize; i++) {
            int slot = availableSlots != null ? availableSlots[i] : i;
            if (!inv.getStack(slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Makes this hopper remember the given inventory.
     *
     * @param extractInventory Block inventory / Blockentity inventory to be remembered
     */
    private void cacheExtractBlockInventory(Inventory extractInventory) {
        assert !(extractInventory instanceof Entity);
        if (extractInventory instanceof BlockEntity || extractInventory instanceof DoubleInventory) {
            this.extractBlockInventory = null;
        } else {
            this.extractBlockInventory = extractInventory == null ? USE_ENTITY_INVENTORY : extractInventory;
        }
        if (extractInventory instanceof LithiumInventory optimizedInventory) {
            this.extractInventory = optimizedInventory;
            LithiumStackList extractInventoryStackList = InventoryHelper.getLithiumStackList(optimizedInventory);
            this.extractInventoryStackList = extractInventoryStackList;
            this.extractInventoryChangeCount = extractInventoryStackList.getModCount() - 1;
            this.extractInventoryRemovedCount = optimizedInventory.getRemovedCountLithium();
        } else {
            this.extractInventory = null;
            this.extractInventoryStackList = null;
            this.extractInventoryChangeCount = 0;
            this.extractInventoryRemovedCount = 0;
        }
    }

    public Inventory getExtractBlockInventory(World world) {
        Inventory inventory = this.extractBlockInventory;
        if (inventory != null) {
            return inventory == USE_ENTITY_INVENTORY ? null : inventory;
        }
        LithiumInventory optimizedInventory;
        if ((optimizedInventory = this.extractInventory) != null) {
            if (optimizedInventory.getRemovedCountLithium() == this.extractInventoryRemovedCount) {
                return optimizedInventory;
            }
        }
        inventory = HopperHelper.vanillaGetBlockInventory(world, this.getPos().up());
        this.cacheExtractBlockInventory(inventory);
        return inventory;
    }

    public Inventory getInsertInventory(World world, BlockState hopperState) {
        Inventory blockInventory = this.getInsertBlockInventory(world, hopperState);
        if (blockInventory != null) {
            return blockInventory;
        }

        if (this.insertInventoryEntityTracker == null) {
            this.initInsertInventoryTracker(world, hopperState);
        }
        if (this.insertInventoryEntityTracker.isUnchangedSince(this.insertInventoryEntityAttemptTime)) {
            return null;
        }
        this.insertInventoryEntityAttemptTime = Long.MIN_VALUE;
        this.myLastCollectChangeCount = InventoryHelper.getLithiumStackList(this).getModCount();

        List<Inventory> inventoryEntities = this.insertInventoryEntityTracker.getEntities(this.insertInventoryEntityBox);
        if (inventoryEntities.isEmpty()) {
            this.insertInventoryEntityAttemptTime = this.lastTickTime;
            //only set unchanged when no entity present. this allows shortcutting this case
            //shortcutting the entity present case requires checking its change counter
            return null;
        }
        Inventory inventory = inventoryEntities.get(world.random.nextInt(inventoryEntities.size()));
        if (inventory != this.insertInventory && inventory instanceof LithiumInventory optimizedInventory) {
            //not caching the inventory (this.insertBlockInventory == USE_ENTITY_INVENTORY prevents it)
            //make change counting on the entity inventory possible, without caching it as block inventory
            this.insertInventory = optimizedInventory;
            this.insertInventoryStackList = InventoryHelper.getLithiumStackList(optimizedInventory);
            this.insertInventoryChangeCount = this.insertInventoryStackList.getModCount() - 1;
        }
        return inventory;
    }

    private void initExtractItemEntityTracker() {
        List<Box> list = new ArrayList<>();
        Box encompassingBox = null;
        for (Box box : this.getInputAreaShape().getBoundingBoxes()) {
            Box offsetBox = box.offset(this.pos.getX(), this.pos.getY(), this.pos.getZ());
            list.add(offsetBox);
            if (encompassingBox == null) {
                encompassingBox = offsetBox;
            } else {
                encompassingBox = encompassingBox.union(offsetBox);
            }
        }
        list.add(encompassingBox);
        this.extractItemEntityBoxes = list.toArray(new Box[0]);
        this.extractItemEntityTracker =
                SectionedItemEntityMovementTracker.getOrCreate(
                        this.world,
                        encompassingBox,
                        ItemEntity.class
                );
        this.extractItemEntityAttemptTime = Long.MIN_VALUE;
        this.extractItemEntityTracker.register((ServerWorld) this.world);
    }

    private void initExtractInventoryTracker(World world) {
        assert world instanceof ServerWorld;
        BlockPos pos = this.pos.offset(Direction.UP);
        this.extractInventoryEntityBox = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
        this.extractInventoryEntityTracker =
                SectionedInventoryEntityMovementTracker.getOrCreate(
                        this.world,
                        this.extractInventoryEntityBox,
                        Inventory.class
                );
        this.extractInventoryEntityAttemptTime = Long.MIN_VALUE;
        this.extractInventoryEntityTracker.register((ServerWorld) world);
    }

    private void initInsertInventoryTracker(World world, BlockState hopperState) {
        assert world instanceof ServerWorld;
        Direction direction = hopperState.get(HopperBlock.FACING);
        BlockPos pos = this.pos.offset(direction);
        this.insertInventoryEntityBox = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
        this.insertInventoryEntityTracker =
                SectionedInventoryEntityMovementTracker.getOrCreate(
                        this.world,
                        this.insertInventoryEntityBox,
                        Inventory.class
                );
        this.insertInventoryEntityAttemptTime = Long.MIN_VALUE;
        this.insertInventoryEntityTracker.register((ServerWorld) world);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setCachedState(BlockState state) {
        BlockState cachedState = this.getCachedState();
        super.setCachedState(state);
        if (state.get(HopperBlock.FACING) != cachedState.get(HopperBlock.FACING)) {
            this.invalidateCachedData();
        }
    }

    @Override
    public void markRemoved() {
        super.markRemoved();
        this.invalidateCachedData();
    }

    private void invalidateCachedData() {
        this.invalidateInsertionData();
        this.invalidateExtractionData();
    }

    private void invalidateInsertionData() {
        if (this.world instanceof ServerWorld serverWorld) {
            if (this.insertInventoryEntityTracker != null) {
                this.insertInventoryEntityTracker.unRegister(serverWorld);
                this.insertInventoryEntityTracker = null;
                this.insertInventoryEntityBox = null;
            }
        }
        this.invalidateBlockInsertionData();
    }

    private void invalidateBlockInsertionData() {
        this.insertBlockInventory = null;
        this.insertInventory = null;
        this.insertInventoryRemovedCount = 0;
        this.insertInventoryStackList = null;
        this.insertInventoryChangeCount = 0;

    }

    private void invalidateExtractionData() {
        if (this.world instanceof ServerWorld serverWorld) {
            if (this.extractInventoryEntityTracker != null) {
                this.extractInventoryEntityTracker.unRegister(serverWorld);
                this.extractInventoryEntityTracker = null;
                this.extractInventoryEntityBox = null;
            }
            if (this.extractItemEntityTracker != null) {
                this.extractItemEntityTracker.unRegister(serverWorld);
                this.extractItemEntityTracker = null;
                this.extractItemEntityBoxes = null;
                this.extractItemEntityTrackerWasEmpty = false;
            }
        }
    }

    private void invalidateBlockExtractionData() {
        this.extractBlockInventory = null;
        this.extractInventory = null;
        this.extractInventoryRemovedCount = 0;
        this.extractInventoryStackList = null;
        this.extractInventoryChangeCount = 0;
    }
}
