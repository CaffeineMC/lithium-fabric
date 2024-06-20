package me.jellysquid.mods.lithium.mixin.world.game_events.dispatch;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import me.jellysquid.mods.lithium.common.world.LithiumData;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.event.listener.GameEventDispatcher;
import net.minecraft.world.gen.chunk.BlendingData;
import net.minecraft.world.tick.ChunkTickScheduler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin extends Chunk {

    @Unique
    private static final Int2ObjectOpenHashMap<?> EMPTY_MAP = new Int2ObjectOpenHashMap<>(0);


    @Shadow
    @Final
    @Mutable
    private Int2ObjectMap<GameEventDispatcher> gameEventDispatchers;

    public WorldChunkMixin(ChunkPos pos, UpgradeData upgradeData, HeightLimitView heightLimitView, Registry<Biome> biomeRegistry, long inhabitedTime, @Nullable ChunkSection[] sectionArray, @Nullable BlendingData blendingData) {
        super(pos, upgradeData, heightLimitView, biomeRegistry, inhabitedTime, sectionArray, blendingData);
    }

    @Shadow
    public abstract World getWorld();

    @Shadow
    private boolean loadedToWorld;

    @Redirect(
            method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/world/chunk/UpgradeData;Lnet/minecraft/world/tick/ChunkTickScheduler;Lnet/minecraft/world/tick/ChunkTickScheduler;J[Lnet/minecraft/world/chunk/ChunkSection;Lnet/minecraft/world/chunk/WorldChunk$EntityLoader;Lnet/minecraft/world/gen/chunk/BlendingData;)V",
            at = @At(value = "NEW", target = "it/unimi/dsi/fastutil/ints/Int2ObjectOpenHashMap", remap = false),
            require = 1, allow = 1
    )
    private Int2ObjectOpenHashMap<?> initGameEventDispatchers() {
        return EMPTY_MAP;
    }
    @Inject(
            method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/world/chunk/UpgradeData;Lnet/minecraft/world/tick/ChunkTickScheduler;Lnet/minecraft/world/tick/ChunkTickScheduler;J[Lnet/minecraft/world/chunk/ChunkSection;Lnet/minecraft/world/chunk/WorldChunk$EntityLoader;Lnet/minecraft/world/gen/chunk/BlendingData;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Heightmap$Type;values()[Lnet/minecraft/world/Heightmap$Type;"),
            require = 1, allow = 1
    )
    private void replaceWithNullMap(World world, ChunkPos pos, UpgradeData upgradeData, ChunkTickScheduler<?> blockTickScheduler, ChunkTickScheduler<?> fluidTickScheduler, long inhabitedTime, ChunkSection[] sectionArrayInitializer, WorldChunk.EntityLoader entityLoader, BlendingData blendingData, CallbackInfo ci) {
        this.setGameEventDispatchers(this.gameEventDispatchers == EMPTY_MAP ? null : this.gameEventDispatchers);
    }

    @Inject(
            method = "getGameEventDispatcher(I)Lnet/minecraft/world/event/listener/GameEventDispatcher;",
            at = @At(value = "FIELD", shift = At.Shift.BEFORE, target = "Lnet/minecraft/world/chunk/WorldChunk;gameEventDispatchers:Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;")
    )
    private void initializeCollection(int ySectionCoord, CallbackInfoReturnable<GameEventDispatcher> cir) {
        if (this.gameEventDispatchers == null) {
            this.setGameEventDispatchers(new Int2ObjectOpenHashMap<>(4));
        }
    }

    @Inject(
            method = "removeGameEventDispatcher(I)V",
            at = @At("RETURN")
    )
    private void removeGameEventDispatcher(int ySectionCoord, CallbackInfo ci) {
        if (this.gameEventDispatchers != null && this.gameEventDispatchers.isEmpty()) {
            this.setGameEventDispatchers(null);
        }
    }

    @Unique
    public void setGameEventDispatchers(Int2ObjectMap<GameEventDispatcher> gameEventDispatchers) {
        if (this.loadedToWorld) {
            this.updateGameEventDispatcherStorage(gameEventDispatchers, this.gameEventDispatchers);
        }

        this.gameEventDispatchers = gameEventDispatchers;
    }

    @Unique
    private void updateGameEventDispatcherStorage(Int2ObjectMap<GameEventDispatcher> newDispatchers, Int2ObjectMap<GameEventDispatcher> expectedDispatchers) {
        Long2ReferenceOpenHashMap<Int2ObjectMap<GameEventDispatcher>> dispatchersByChunk =
                ((LithiumData) this.getWorld()).lithium$getData().gameEventDispatchersByChunk();
        Int2ObjectMap<GameEventDispatcher> removedDispatchers;
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
            method = "setLoadedToWorld(Z)V", at = @At("RETURN")
    )
    private void handleLoadOrUnload(boolean loadedToWorld, CallbackInfo ci) {
        if (this.gameEventDispatchers == null) {
            return;
        }
        if (loadedToWorld) {
            this.updateGameEventDispatcherStorage(this.gameEventDispatchers, null);
        } else {
            this.updateGameEventDispatcherStorage(null, this.gameEventDispatchers);
        }
    }
}
