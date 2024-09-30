package me.jellysquid.mods.lithium.mixin.world.game_events.dispatch;

import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import me.jellysquid.mods.lithium.common.util.ChunkConstants;
import me.jellysquid.mods.lithium.common.world.LithiumData;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventDispatcher;
import net.minecraft.world.level.gameevent.GameEventListenerRegistry;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameEventDispatcher.class)
public class GameEventDispatchManagerMixin {

    @Shadow
    @Final
    private ServerLevel level;

    @Redirect(
            method = "post",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerChunkCache;getChunkNow(II)Lnet/minecraft/world/level/chunk/LevelChunk;"
            )
    )
    private LevelChunk doNotGetChunk(ServerChunkCache instance, int chunkX, int chunkZ){
        return ChunkConstants.DUMMY_CHUNK;
    }

    @Redirect(
            method = "post",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/chunk/ChunkAccess;getListenerRegistry(I)Lnet/minecraft/world/level/gameevent/GameEventListenerRegistry;"
            )
    )
    private GameEventListenerRegistry getNull(ChunkAccess chunk, int ySectionCoord) {
        return null;
    }

    @Redirect(
            method = "post",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/gameevent/GameEventListenerRegistry;visitInRangeListeners(Lnet/minecraft/core/Holder;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/level/gameevent/GameEvent$Context;Lnet/minecraft/world/level/gameevent/GameEventListenerRegistry$ListenerVisitor;)Z"
            )
    )
    private boolean handleNullDispatcher(@Nullable GameEventListenerRegistry dispatcher, Holder<GameEvent> gameEventRegistryEntry, Vec3 vec3d, GameEvent.Context emitter, GameEventListenerRegistry.ListenerVisitor dispatchCallback, @Local(ordinal = 7) int chunkX, @Local(ordinal = 8) int chunkZ, @Local(ordinal = 9) int ySectionCoord) {
        if (dispatcher == null) {
            Int2ObjectMap<GameEventListenerRegistry> yToDispatcherMap = ((LithiumData) this.level).lithium$getData().gameEventDispatchersByChunk().get(ChunkPos.asLong(chunkX, chunkZ));
            dispatcher = yToDispatcherMap == null ? null : yToDispatcherMap.get(ySectionCoord);
        }
        return dispatcher != null && dispatcher.visitInRangeListeners(gameEventRegistryEntry, vec3d, emitter, dispatchCallback);
    }
}
