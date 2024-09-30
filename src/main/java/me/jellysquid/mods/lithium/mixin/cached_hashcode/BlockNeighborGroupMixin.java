package me.jellysquid.mods.lithium.mixin.cached_hashcode;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.BlockStatePairKey.class)
public class BlockNeighborGroupMixin {
    @Shadow
    @Final
    private BlockState first;

    @Shadow
    @Final
    private BlockState second;

    @Shadow
    @Final
    private Direction direction;

    private int hash;

    /**
     * @reason Initialize the object's hashcode and cache it
     */
    @Inject(method = "<init>(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)V", at = @At("RETURN"))
    private void generateHash(BlockState blockState_1, BlockState blockState_2, Direction direction_1, CallbackInfo ci) {
        int hash = this.first.hashCode();
        hash = 31 * hash + this.second.hashCode();
        hash = 31 * hash + this.direction.hashCode();

        this.hash = hash;
    }

    /**
     * @reason Uses the cached hashcode
     * @author JellySquid
     */
    @Overwrite(remap = false)
    public int hashCode() {
        return this.hash;
    }
}
