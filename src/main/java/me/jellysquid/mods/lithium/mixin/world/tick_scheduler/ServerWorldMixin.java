package me.jellysquid.mods.lithium.mixin.world.tick_scheduler;

import me.jellysquid.mods.lithium.common.world.scheduler.LithiumServerTickScheduler;
import net.minecraft.server.world.ServerTickScheduler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.ScheduledTick;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
    /**
     * Redirects the creation of the vanilla server tick scheduler with our own. This only happens once per world load.
     */
    @Redirect(
            method = "<init>(Lnet/minecraft/server/MinecraftServer;Ljava/util/concurrent/Executor;Lnet/minecraft/world/level/storage/LevelStorage$Session;Lnet/minecraft/world/level/ServerWorldProperties;Lnet/minecraft/util/registry/RegistryKey;Lnet/minecraft/world/dimension/DimensionType;Lnet/minecraft/server/WorldGenerationProgressListener;Lnet/minecraft/world/gen/chunk/ChunkGenerator;ZJLjava/util/List;Z)V",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/server/world/ServerTickScheduler"
            )
    )
    private <T> ServerTickScheduler<T> redirectServerTickSchedulerCtor(ServerWorld world, Predicate<T> invalidPredicate, Function<T, Identifier> idToName, Consumer<ScheduledTick<T>> tickConsumer) {
        return new LithiumServerTickScheduler<>(world, invalidPredicate, idToName, tickConsumer);
    }
}

