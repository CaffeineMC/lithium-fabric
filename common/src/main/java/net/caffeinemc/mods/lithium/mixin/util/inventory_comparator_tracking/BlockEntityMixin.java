package net.caffeinemc.mods.lithium.mixin.util.inventory_comparator_tracking;

import net.caffeinemc.mods.lithium.common.block.entity.inventory_change_tracking.InventoryChangeTracker;
import net.caffeinemc.mods.lithium.common.block.entity.inventory_comparator_tracking.ComparatorTracker;
import net.caffeinemc.mods.lithium.common.block.entity.inventory_comparator_tracking.ComparatorTracking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
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
    protected Level level;
    @Shadow
    @Final
    protected BlockPos worldPosition;
    private static final byte UNKNOWN = (byte) -1;
    private static final byte COMPARATOR_PRESENT = (byte) 1;
    private static final byte COMPARATOR_ABSENT = (byte) 0;

    byte hasComparators;

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void init(BlockEntityType<?> type, BlockPos pos, BlockState state, CallbackInfo ci) {
        this.hasComparators = UNKNOWN;
    }

    @Override
    public void lithium$onComparatorAdded(Direction direction, int offset) {
        byte hasComparators = this.hasComparators;
        if (direction.getAxis() != Direction.Axis.Y && hasComparators != COMPARATOR_PRESENT && offset >= 1 && offset <= 2) {
            this.hasComparators = COMPARATOR_PRESENT;

            if (this instanceof InventoryChangeTracker inventoryChangeTracker) {
                inventoryChangeTracker.lithium$emitFirstComparatorAdded();
            }
        }
    }

    @Override
    public boolean lithium$hasAnyComparatorNearby() {
        if (this.hasComparators == UNKNOWN) {
            this.hasComparators = ComparatorTracking.findNearbyComparators(this.level, this.worldPosition) ? COMPARATOR_PRESENT : COMPARATOR_ABSENT;
        }
        return this.hasComparators == COMPARATOR_PRESENT;
    }

    @Inject(method = "setRemoved()V", at = @At("HEAD"))
    private void forgetNearbyComparators(CallbackInfo ci) {
        //Compatibility with mods that move block entities (e.g. fabric-carpet)
        this.hasComparators = UNKNOWN;
    }
}
