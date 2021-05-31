package me.jellysquid.mods.lithium.mixin.block.hopper;

import me.jellysquid.mods.lithium.common.hopper.LithiumStackList;
import me.jellysquid.mods.lithium.common.hopper.StorableItemStack;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemStack.class)
public class ItemStackMixin implements StorableItemStack {
    @Shadow
    private int count;
    @Nullable
    private LithiumStackList myLocation;

    @Override
    public void registerToInventory(LithiumStackList itemStacks) {
        assert this.myLocation == null;
        this.myLocation = itemStacks;
    }

    @Override
    public void unregisterFromInventory(LithiumStackList myInventoryList) {
        assert this.myLocation == myInventoryList;
        this.myLocation = null;
    }

    @ModifyVariable(method = "setCount", at = @At("HEAD"))
    public int updateInventory(int count) {
        if (this.myLocation != null && this.count != count) {
            this.myLocation.changed();
        }
        return count;
    }
}
