package me.jellysquid.mods.lithium.mixin.entity.item_self_insert;

import me.jellysquid.mods.lithium.common.blockentities.HopperAccess;
import me.jellysquid.mods.lithium.common.entity.ExtendedEntity;
import me.jellysquid.mods.lithium.common.util.chunks.Chunks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {

    public ItemEntityMixin(EntityType<? extends ItemEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void tick(CallbackInfo ci) {
        if (!this.world.isClient) {
            Box box = this.getBoundingBox();
            int y = (int) (this.y - 1); // we want only hoppers underneath the item, hoppers already deal with items that are physically colliding with itself, or atleast in theory

            for (int x = MathHelper.floor(box.minX); x <= MathHelper.floor(box.maxX); x++) { // we want to check all the hoppers that intersect with the item's
                for (int z = MathHelper.floor(box.minZ); z <= MathHelper.floor(box.maxZ); z++) { // hitbox/bounding box
                    if (this.removed) { // check if the item hs been removed
                        return;
                    }
                    WorldChunk chunk = ((ExtendedEntity) this).getEntityChunkCache().getWorldChunk(x >> 4, z >> 4);
                    this.extract(chunk, new BlockPos(x, y, z));
                }
            }

            // make sure we aren't inside a hopper
            BlockPos pos = this.getBlockPos();
            WorldChunk chunk = ((ExtendedEntity) this).getEntityChunkCache().getWorldChunk(this);
            BlockEntity entity = chunk.getBlockEntity(pos);
            if (entity instanceof Hopper && ((HopperAccess) entity).shouldAcceptItems() && Hopper.INSIDE_SHAPE.getBoundingBox().offset(this.getBlockPos()).intersects(this.getBoundingBox()) && Chunks.getInventoryInChunk(chunk, pos.add(0, 1, 0)) == null/* must check if there's an inventory above just in case, in order to better replicate vanilla behavior*/) {// check if hopper is on cooldown
                HopperBlockEntity.extract((Inventory) entity, (ItemEntity) (Object) this); // yeet ourselves into the hopper
                if (this.removed) { // if I was consumed we have to increase the hopper's cooldown
                    ((HopperAccess) entity).setCool(8);
                }
            }
        }
    }

    private void extract(WorldChunk chunk, BlockPos pos) {
        BlockEntity entity = chunk.getBlockEntity(pos);
        if (entity instanceof Hopper && ((HopperAccess) entity).shouldAcceptItems() && Chunks.getInventoryInChunk(chunk, pos.add(0, 1, 0)) == null/* must check if there's an inventory above just in case, in order to better replicate vanilla behavior*/) {// check if hopper is on cooldown
            HopperBlockEntity.extract((Inventory) entity, (ItemEntity) (Object) this); // yeet ourselves into the hopper
            if (this.removed) { // if I was consumed we have to increase the hopper's cooldown
                ((HopperAccess) entity).setCool(8);
            }
        }
    }
}
