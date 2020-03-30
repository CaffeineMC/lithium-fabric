package me.jellysquid.mods.lithium.mixin.world.fast_tick_scheduler;

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
public abstract class MixinServerWorld {
    /**
     * Redirects the creation of the vanilla server tick scheduler with our own.
     */
    @Redirect(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/server/world/ServerTickScheduler"))
    private <T> ServerTickScheduler<T> redirectServerTickSchedulerCtor(ServerWorld world, Predicate<T> invalidPredicate, Function<T, Identifier> idToName, Consumer<ScheduledTick<T>> tickConsumer) {
        return new LithiumServerTickScheduler<>(world, invalidPredicate, idToName, tickConsumer);
    }
}

