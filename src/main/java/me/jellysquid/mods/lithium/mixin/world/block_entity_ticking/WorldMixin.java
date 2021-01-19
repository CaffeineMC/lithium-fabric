package me.jellysquid.mods.lithium.mixin.world.block_entity_ticking;

import me.jellysquid.mods.lithium.common.util.collections.BlockEntityList;
import me.jellysquid.mods.lithium.common.util.collections.HashedReferenceList;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

@Mixin(World.class)
public abstract class WorldMixin implements WorldAccess {
    @Shadow
    @Final
    public boolean isClient;
    @Mutable
    @Shadow
    @Final
    public List<BlockEntity> blockEntities;

    @Mutable
    @Shadow
    @Final
    public List<BlockEntity> tickingBlockEntities;

    @Mutable
    @Shadow
    @Final
    protected List<BlockEntity> pendingBlockEntities;

    @Shadow
    @Final
    private Supplier<Profiler> profiler;

    private BlockEntityList blockEntities$lithium;
    private BlockEntityList pendingBlockEntities$lithium;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void reinit(MutableWorldProperties properties, RegistryKey<World> registryKey, DimensionType dimensionType,
                        Supplier<Profiler> supplier, boolean bl, boolean bl2, long l, CallbackInfo ci) {
        // Replace the fallback collections with our types as well
        // This won't guarantee mod compatibility, but at least it should fail loudly when it does
        this.blockEntities$lithium = new BlockEntityList(this.blockEntities, false);
        this.blockEntities = this.blockEntities$lithium;

        this.tickingBlockEntities = new HashedReferenceList<>(this.tickingBlockEntities);

        this.pendingBlockEntities$lithium = new BlockEntityList(this.pendingBlockEntities, true);
        this.pendingBlockEntities = this.pendingBlockEntities$lithium;
    }

    /**
     * @author JellySquid
     * @reason Replace with direct lookup
     */
    @SuppressWarnings("OverwriteModifiers")
    @Overwrite
    private BlockEntity getPendingBlockEntity(BlockPos pos) {
        return this.pendingBlockEntities$lithium.getFirstNonRemovedBlockEntityAtPosition(pos.asLong());
    }

    // We do not want the vanilla code for adding pending block entities to be ran. We'll inject later in
    // postBlockEntityTick to use our optimized implementation.
    @Redirect(method = "tickBlockEntities", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;pendingBlockEntities:Ljava/util/List;", ordinal = 0))
    private List<?> nullifyPendingBlockEntityListDuringTick(World world) {
        return Collections.emptyList();
    }

    // Add any pending block entities to the world.
    @Inject(method = "tickBlockEntities", at = @At("RETURN"))
    private void postBlockEntityTick(CallbackInfo ci) {
        Profiler profiler = this.profiler.get();
        profiler.push("pendingBlockEntities$lithium");

        // The usage of a for-index loop is invalid with our optimized implementation, so use an iterator here
        // The overhead of this is essentially non-zero and doesn't matter in this code.
        for (BlockEntity entity : this.pendingBlockEntities) {
            if (entity.isRemoved()) {
                continue;
            }

            // Try-add directly to avoid the double map lookup, helps speed things along
            if (this.blockEntities$lithium.addIfAbsent(entity)) {
                //vanilla has an extra updateListeners(...) call on the client here, but the one below should be enough
                if (entity instanceof Tickable) {
                    this.tickingBlockEntities.add(entity);
                }

                BlockPos pos = entity.getPos();

                // Avoid the double chunk lookup (isLoaded followed by getChunk) by simply inlining getChunk call
                // pass this.isClient instead of false, so the updateListeners call is always executed on the client (like vanilla)
                Chunk chunk = this.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, this.isClient);

                if (chunk != null) {
                    BlockState state = chunk.getBlockState(pos);
                    chunk.setBlockEntity(pos, entity);

                    this.updateListeners(pos, state, state, 3);
                }
            }
        }

        this.pendingBlockEntities.clear();

        profiler.pop();
    }

    // We don't want this code wasting a ton of CPU time trying to scan through our optimized collection
    // Instead, we simply run the code on those at the same position directly
    @Redirect(method = "setBlockEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BlockEntity;setLocation(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"))
    private void setLocationAndRemoveAllAtPosition(BlockEntity blockEntity, World world, BlockPos pos) {
        blockEntity.setLocation(world, pos);
        this.pendingBlockEntities$lithium.markRemovedAndRemoveAllAtPosition(pos);
    }
    @Redirect(method = "setBlockEntity", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"))
    private <E> Iterator<E> nullifyBlockEntityScanDuringSetBlockEntity(List<E> list) {
        return Collections.emptyIterator();
    }

    @Shadow
    public abstract void updateListeners(BlockPos pos, BlockState oldState, BlockState newState, int flags);
}
