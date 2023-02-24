package me.jellysquid.mods.lithium.mixin.block.hopper.worldedit_compat;

import me.jellysquid.mods.lithium.common.compat.worldedit.WorldEditCompat;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin {

    @Inject(
            method = "setBlockEntity(Lnet/minecraft/block/entity/BlockEntity;)V",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")
    )
    private void updateHoppersIfWorldEditPresent(BlockEntity blockEntity, CallbackInfo ci) {
        if (WorldEditCompat.WORLD_EDIT_PRESENT && blockEntity instanceof Inventory) {
            WorldEditCompat.updateHopperCachesOnNewInventoryAdded((WorldChunk) (Object) this, blockEntity);
        }
    }
}
