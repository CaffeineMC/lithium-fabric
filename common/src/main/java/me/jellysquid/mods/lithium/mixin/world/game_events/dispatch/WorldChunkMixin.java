package me.jellysquid.mods.lithium.mixin.world.game_events.dispatch;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import me.jellysquid.mods.lithium.common.world.LithiumData;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.gameevent.GameEventListenerRegistry;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.ticks.LevelChunkTicks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
public abstract class WorldChunkMixin extends ChunkAccess {

    @Unique
    private static final Int2ObjectOpenHashMap<?> EMPTY_MAP = new Int2ObjectOpenHashMap<>(0);


    @Shadow
    @Final
    @Mutable
    private Int2ObjectMap<GameEventListenerRegistry> gameEventListenerRegistrySections;

    public WorldChunkMixin(ChunkPos pos, UpgradeData upgradeData, LevelHeightAccessor heightLimitView, Registry<Biome> biomeRegistry, long inhabitedTime, @Nullable LevelChunkSection[] sectionArray, @Nullable BlendingData blendingData) {
        super(pos, upgradeData, heightLimitView, biomeRegistry, inhabitedTime, sectionArray, blendingData);
    }

    @Shadow
    public abstract Level getLevel();

    @Shadow
    private boolean loaded;

    @Redirect(
            method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/UpgradeData;Lnet/minecraft/world/ticks/LevelChunkTicks;Lnet/minecraft/world/ticks/LevelChunkTicks;J[Lnet/minecraft/world/level/chunk/LevelChunkSection;Lnet/minecraft/world/level/chunk/LevelChunk$PostLoadProcessor;Lnet/minecraft/world/level/levelgen/blending/BlendingData;)V",
            at = @At(value = "NEW", target = "it/unimi/dsi/fastutil/ints/Int2ObjectOpenHashMap", remap = false),
            require = 1, allow = 1
    )
    private Int2ObjectOpenHashMap<?> initGameEventDispatchers() {
        return EMPTY_MAP;
    }
    @Inject(
            method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/UpgradeData;Lnet/minecraft/world/ticks/LevelChunkTicks;Lnet/minecraft/world/ticks/LevelChunkTicks;J[Lnet/minecraft/world/level/chunk/LevelChunkSection;Lnet/minecraft/world/level/chunk/LevelChunk$PostLoadProcessor;Lnet/minecraft/world/level/levelgen/blending/BlendingData;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/Heightmap$Types;values()[Lnet/minecraft/world/level/levelgen/Heightmap$Types;"),
            require = 1, allow = 1
    )
    private void replaceWithNullMap(Level world, ChunkPos pos, UpgradeData upgradeData, LevelChunkTicks<?> blockTickScheduler, LevelChunkTicks<?> fluidTickScheduler, long inhabitedTime, LevelChunkSection[] sectionArrayInitializer, LevelChunk.PostLoadProcessor entityLoader, BlendingData blendingData, CallbackInfo ci) {
        this.setGameEventListenerRegistrySections(this.gameEventListenerRegistrySections == EMPTY_MAP ? null : this.gameEventListenerRegistrySections);
    }

    @Inject(
            method = "getListenerRegistry(I)Lnet/minecraft/world/level/gameevent/GameEventListenerRegistry;",
            at = @At(value = "FIELD", shift = At.Shift.BEFORE, target = "Lnet/minecraft/world/level/chunk/LevelChunk;gameEventListenerRegistrySections:Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;")
    )
    private void initializeCollection(int ySectionCoord, CallbackInfoReturnable<GameEventListenerRegistry> cir) {
        if (this.gameEventListenerRegistrySections == null) {
            this.setGameEventListenerRegistrySections(new Int2ObjectOpenHashMap<>(4));
        }
    }

    @Inject(
            method = "removeGameEventListenerRegistry",
            at = @At("RETURN")
    )
    private void removeGameEventDispatcher(int ySectionCoord, CallbackInfo ci) {
        if (this.gameEventListenerRegistrySections != null && this.gameEventListenerRegistrySections.isEmpty()) {
            this.setGameEventListenerRegistrySections(null);
        }
    }

    @Unique
    public void setGameEventListenerRegistrySections(Int2ObjectMap<GameEventListenerRegistry> gameEventListenerRegistrySections) {
        if (this.loaded) {
            this.updateGameEventDispatcherStorage(gameEventListenerRegistrySections, this.gameEventListenerRegistrySections);
        }

        this.gameEventListenerRegistrySections = gameEventListenerRegistrySections;
    }

    @Unique
    private void updateGameEventDispatcherStorage(Int2ObjectMap<GameEventListenerRegistry> newDispatchers, Int2ObjectMap<GameEventListenerRegistry> expectedDispatchers) {
        Long2ReferenceOpenHashMap<Int2ObjectMap<GameEventListenerRegistry>> dispatchersByChunk =
                ((LithiumData) this.getLevel()).lithium$getData().gameEventDispatchersByChunk();
        Int2ObjectMap<GameEventListenerRegistry> removedDispatchers;
        if (newDispatchers != null) {
            removedDispatchers = dispatchersByChunk.put(this.getPos().toLong(), newDispatchers);
        } else {
            removedDispatchers = dispatchersByChunk.remove(this.getPos().toLong());
        }
        if (removedDispatchers != expectedDispatchers) {
            throw new IllegalStateException("Wrong game event dispatcher map found in lithium storage: " + removedDispatchers + " (expected " + expectedDispatchers + ")");
        }
    }

    @Inject(
            method = "setLoaded(Z)V", at = @At("RETURN")
    )
    private void handleLoadOrUnload(boolean loadedToWorld, CallbackInfo ci) {
        if (this.gameEventListenerRegistrySections == null) {
            return;
        }
        if (loadedToWorld) {
            this.updateGameEventDispatcherStorage(this.gameEventListenerRegistrySections, null);
        } else {
            this.updateGameEventDispatcherStorage(null, this.gameEventListenerRegistrySections);
        }
    }
}
