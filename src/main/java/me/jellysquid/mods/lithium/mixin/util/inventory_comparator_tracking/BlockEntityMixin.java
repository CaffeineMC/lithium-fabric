package me.jellysquid.mods.lithium.mixin.util.inventory_comparator_tracking;

import me.jellysquid.mods.lithium.common.block.entity.inventory_change_tracking.InventoryChangeTracker;
import me.jellysquid.mods.lithium.common.block.entity.inventory_comparator_tracking.ComparatorTracker;
import me.jellysquid.mods.lithium.common.block.entity.inventory_comparator_tracking.ComparatorTracking;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public class BlockEntityMixin implements ComparatorTracker {
    //This mixin is meant for all block entities that also implement the inventory Inventory interface
    //Applying this mixin to all block entities seems to be the best solution to edit all of them with the same code

    @Shadow
    @Nullable
    protected World world;
    @Shadow
    @Final
    protected BlockPos pos;
    private static final byte UNKNOWN = (byte) 0b11111111;

    //Keep track of nearby comparators
    //0b11111111 : unknown, not initalized
    //Anything else:
    //0bEEWWSSNN : (Direction.id - 2)
    //For each direction: N = North, etc.
    //01 = comparator is directly next to the block entity in that direction
    //10 = comparator is NOT directly next to the block entity but one block far away in that direction
    //00 = no comparator nearby
    //11 never occurs (!) -> allows having a code for unknown

    byte comparatorsNearby;

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void init(BlockEntityType<?> type, BlockPos pos, BlockState state, CallbackInfo ci) {
        this.comparatorsNearby = UNKNOWN;
    }

    //TODO either use the location of the comparators or use only 3 states (uninitalized, present, absent)


    @Override
    public void onComparatorAdded(Direction direction, int offset) {
        byte comparatorsNearby = this.comparatorsNearby;
        if (comparatorsNearby != UNKNOWN && direction.getAxis() != Direction.Axis.Y && offset >= 1 && offset <= 2) {
            this.comparatorsNearby = (byte) (comparatorsNearby | (1 << ((direction.getId() - 2) * 2 + (offset - 1))));
        }

        if (this instanceof InventoryChangeTracker inventoryChangeTracker) {
            inventoryChangeTracker.emitComparatorAdded();
        }
    }

    @Override
    public boolean hasAnyComparatorNearby() {
        if (this.comparatorsNearby == UNKNOWN) {
            this.comparatorsNearby = ComparatorTracking.findNearbyComparators(this.world, this.pos);
        }
        return this.comparatorsNearby != (byte) 0;
    }

    @Inject(method = "markRemoved()V", at = @At("HEAD" ))
    private void forgetNearbyComparators(CallbackInfo ci) {
        //Compatibility with mods that move block entities (e.g. fabric-carpet)
        this.comparatorsNearby = UNKNOWN;
    }
}
