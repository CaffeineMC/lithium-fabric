package me.jellysquid.mods.lithium.mixin.entity.item_self_insert;

import me.jellysquid.mods.lithium.common.blockentities.HopperAccess;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin (ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {

	public ItemEntityMixin(EntityType<? extends ItemEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject (method = "tick", at = @At ("RETURN"))
	public void tick(CallbackInfo ci) {
		int y = (int) (this.y - 1); // we want only hoppers underneath the item, hoppers already deal with items that are physically colliding with itself, or atleast in theory
		Box box = this.getBoundingBox();
		exit:
		for (double x = box.minX; x < box.maxX; x++) // we want to check all the hoppers that intersect with the following item
			for (double z = box.minZ; z < box.maxZ; z++) {
			    if(this.removed)
			        break exit;
				BlockEntity entity = this.world.getBlockEntity(new BlockPos(x, y, z)); // check if the block entity underneath it is a hopper
				if (entity instanceof HopperBlockEntity && ((HopperAccess)entity).enabled()) {// check if hopper is on cooldown
				    HopperBlockEntity.extract((Inventory) entity, (ItemEntity) (Object) this); // yeet ourselves into the hopper
                }
			}
	}
}
