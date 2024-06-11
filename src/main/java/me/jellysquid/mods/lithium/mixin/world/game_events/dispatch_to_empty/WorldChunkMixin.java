package me.jellysquid.mods.lithium.mixin.world.game_events.dispatch_to_empty;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.jellysquid.mods.lithium.common.world.chunk.ChunkWithEmptyGameEventDispatcher;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.event.listener.GameEventDispatcher;
import net.minecraft.world.gen.chunk.BlendingData;
import net.minecraft.world.tick.ChunkTickScheduler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldChunk.class)
public class WorldChunkMixin implements ChunkWithEmptyGameEventDispatcher {

    private static final Int2ObjectOpenHashMap<?> EMPTY_MAP = new Int2ObjectOpenHashMap<>(0);

    @Shadow
    @Final
    @Mutable
    private Int2ObjectMap<GameEventDispatcher> gameEventDispatchers;

    @Redirect(
            method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/world/chunk/UpgradeData;Lnet/minecraft/world/tick/ChunkTickScheduler;Lnet/minecraft/world/tick/ChunkTickScheduler;J[Lnet/minecraft/world/chunk/ChunkSection;Lnet/minecraft/world/chunk/WorldChunk$EntityLoader;Lnet/minecraft/world/gen/chunk/BlendingData;)V",
            at = @At(value = "NEW", target = "it/unimi/dsi/fastutil/ints/Int2ObjectOpenHashMap"),
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
        if (this.gameEventDispatchers == EMPTY_MAP) {
            this.gameEventDispatchers = null;
        }
    }

    @Override
    public @Nullable GameEventDispatcher lithium$getExistingGameEventDispatcher(int ySectionCoord) {
        if (this.gameEventDispatchers != null) {
            return this.gameEventDispatchers.get(ySectionCoord);
        }
        return null;
    }

    @Inject(
            method = "getGameEventDispatcher(I)Lnet/minecraft/world/event/listener/GameEventDispatcher;",
            at = @At(value = "FIELD", shift = At.Shift.BEFORE, target = "Lnet/minecraft/world/chunk/WorldChunk;gameEventDispatchers:Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;")
    )
    private void initializeCollection(int ySectionCoord, CallbackInfoReturnable<GameEventDispatcher> cir) {
        if (this.gameEventDispatchers == null) {
            this.gameEventDispatchers = new Int2ObjectOpenHashMap<>(4);
        }
    }

    @Inject(
            method = "removeGameEventDispatcher(I)V",
            at = @At("RETURN")
    )
    private void removeGameEventDispatcher(int ySectionCoord, CallbackInfo ci) {
        if (this.gameEventDispatchers != null && this.gameEventDispatchers.isEmpty()) {
            this.gameEventDispatchers = null;
        }
    }
}
