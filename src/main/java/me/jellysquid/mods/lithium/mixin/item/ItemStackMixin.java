package me.jellysquid.mods.lithium.mixin.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    //Optimization: As ItemStack already caches whether it is empty, we only have to actually use the cached value.
    @Shadow
    private boolean empty;
    @Shadow
    private int count;
    @Shadow
    @Final
    private Item item;

    /**
     * @author 2No2Name
     * @reason use cached empty state
     */
    @Overwrite
    public boolean isEmpty() {
        return this.empty;
    }

    @Redirect(method = "updateEmptyState()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"))
    private boolean isEmptyRecalculate(ItemStack itemStack) {
        return (this.item == null || this.item == Items.AIR || this.count <= 0);
    }
}