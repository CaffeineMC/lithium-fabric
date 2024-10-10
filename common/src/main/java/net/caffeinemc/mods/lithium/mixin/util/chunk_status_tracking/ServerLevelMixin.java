package net.caffeinemc.mods.lithium.mixin.util.chunk_status_tracking;

import net.caffeinemc.mods.lithium.common.world.chunk.ChunkStatusTracker;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {

    @ModifyArg(
            method = "<init>",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerChunkCache;<init>(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplateManager;Ljava/util/concurrent/Executor;Lnet/minecraft/world/level/chunk/ChunkGenerator;IIZLnet/minecraft/server/level/progress/ChunkProgressListener;Lnet/minecraft/world/level/entity/ChunkStatusUpdateListener;Ljava/util/function/Supplier;)V")
    )
    private ChunkStatusUpdateListener combineWithLithiumChunkStatusTracker(ChunkStatusUpdateListener previousListener) {
        ServerLevel world = (ServerLevel) (Object) this;
        return (pos, levelType) -> {
            previousListener.onChunkStatusChange(pos, levelType);
            ChunkStatusTracker.onChunkStatusChange(world, pos, levelType);
        };
    }
}
