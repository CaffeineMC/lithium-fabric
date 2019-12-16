package me.jellysquid.mods.lithium.mixin.entity.chunk_cache;

import me.jellysquid.mods.lithium.common.cache.EntityChunkCache;
import me.jellysquid.mods.lithium.common.entity.EntityWithChunkCache;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("InvalidMemberReference")
@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {
    @Redirect(method = {"baseTick", "onDeath", "method_16212", "method_6077", "handleFallDamage", "method_6038", "travel"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"))
    private BlockState redirectGetBlockState(World world, BlockPos pos) {
        EntityChunkCache cache = ((EntityWithChunkCache) this).getEntityChunkCache();
        return cache == null ? world.getBlockState(pos) : cache.getBlockState(pos);
    }

    @Redirect(method = {"method_6038"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getFluidState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/FluidState;"))
    private FluidState redirectGetFluidState(World world, BlockPos pos) {
        EntityChunkCache cache = ((EntityWithChunkCache) this).getEntityChunkCache();
        return cache == null ? world.getFluidState(pos) : cache.getFluidState(pos);
    }
}