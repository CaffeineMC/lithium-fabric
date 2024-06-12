package me.jellysquid.mods.lithium.mixin.world.game_events.dispatch;

import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import me.jellysquid.mods.lithium.common.util.ChunkConstants;
import me.jellysquid.mods.lithium.common.world.LithiumData;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.GameEventDispatchManager;
import net.minecraft.world.event.listener.GameEventDispatcher;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameEventDispatchManager.class)
public class GameEventDispatchManagerMixin {

    @Shadow
    @Final
    private ServerWorld world;

    @Redirect(
            method = "dispatch",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerChunkManager;getWorldChunk(II)Lnet/minecraft/world/chunk/WorldChunk;"
            )
    )
    private WorldChunk doNotGetChunk(ServerChunkManager instance, int chunkX, int chunkZ){
        return ChunkConstants.DUMMY_CHUNK;
    }

    @Redirect(
            method = "dispatch",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/Chunk;getGameEventDispatcher(I)Lnet/minecraft/world/event/listener/GameEventDispatcher;"
            )
    )
    private GameEventDispatcher getNull(Chunk chunk, int ySectionCoord) {
        return null;
    }

    @Redirect(
            method = "dispatch",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/event/listener/GameEventDispatcher;dispatch(Lnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/world/event/GameEvent$Emitter;Lnet/minecraft/world/event/listener/GameEventDispatcher$DispatchCallback;)Z"
            )
    )
    private boolean handleNullDispatcher(@Nullable GameEventDispatcher dispatcher, RegistryEntry<GameEvent> gameEventRegistryEntry, Vec3d vec3d, GameEvent.Emitter emitter, GameEventDispatcher.DispatchCallback dispatchCallback, @Local(ordinal = 7) int chunkX, @Local(ordinal = 8) int chunkZ, @Local(ordinal = 9) int ySectionCoord) {
        if (dispatcher == null) {
            Int2ObjectMap<GameEventDispatcher> yToDispatcherMap = ((LithiumData) this.world).lithium$getData().gameEventDispatchersByChunk().get(ChunkPos.toLong(chunkX, chunkZ));
            dispatcher = yToDispatcherMap == null ? null : yToDispatcherMap.get(ySectionCoord);
        }
        return dispatcher != null && dispatcher.dispatch(gameEventRegistryEntry, vec3d, emitter, dispatchCallback);
    }
}
