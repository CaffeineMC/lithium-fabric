package me.jellysquid.mods.lithium.mixin.world.game_events.dispatch_to_empty;

import me.jellysquid.mods.lithium.common.world.chunk.ChunkWithEmptyGameEventDispatcher;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.GameEventDispatchManager;
import net.minecraft.world.event.listener.GameEventDispatcher;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameEventDispatchManager.class)
public class GameEventDispatchManagerMixin {

    @Redirect(
            method = "dispatch",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/Chunk;getGameEventDispatcher(I)Lnet/minecraft/world/event/listener/GameEventDispatcher;"
            )
    )
    private GameEventDispatcher existingGameEventDispatcherOrNull(Chunk chunk, int ySectionCoord) {
        if (chunk instanceof ChunkWithEmptyGameEventDispatcher)
            return ((ChunkWithEmptyGameEventDispatcher) chunk).lithium$getExistingGameEventDispatcher(ySectionCoord);
        return chunk.getGameEventDispatcher(ySectionCoord);
    }
    @Redirect(
            method = "dispatch",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/event/listener/GameEventDispatcher;dispatch(Lnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/world/event/GameEvent$Emitter;Lnet/minecraft/world/event/listener/GameEventDispatcher$DispatchCallback;)Z"
            )
    )
    private boolean handleNullDispatcher(@Nullable GameEventDispatcher dispatcher, RegistryEntry<GameEvent> gameEventRegistryEntry, Vec3d vec3d, GameEvent.Emitter emitter, GameEventDispatcher.DispatchCallback dispatchCallback) {
        if (dispatcher == null) {
            return false;
        }
        return dispatcher.dispatch(gameEventRegistryEntry, vec3d, emitter, dispatchCallback);
    }
}
