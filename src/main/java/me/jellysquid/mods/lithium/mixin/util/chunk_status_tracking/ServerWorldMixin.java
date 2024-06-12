package me.jellysquid.mods.lithium.mixin.util.chunk_status_tracking;

import me.jellysquid.mods.lithium.common.world.chunk.ChunkStatusTracker;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.ChunkStatusChangeListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {

    @ModifyArg(
            method = "<init>",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerChunkManager;<init>(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/level/storage/LevelStorage$Session;Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/structure/StructureTemplateManager;Ljava/util/concurrent/Executor;Lnet/minecraft/world/gen/chunk/ChunkGenerator;IIZLnet/minecraft/server/WorldGenerationProgressListener;Lnet/minecraft/world/chunk/ChunkStatusChangeListener;Ljava/util/function/Supplier;)V")
    )
    private ChunkStatusChangeListener combineWithLithiumChunkStatusTracker(ChunkStatusChangeListener previousListener) {
        ServerWorld world = (ServerWorld) (Object) this;
        return (pos, levelType) -> {
            previousListener.onChunkStatusChange(pos, levelType);
            ChunkStatusTracker.onChunkStatusChange(world, pos, levelType);
        };
    }
}
