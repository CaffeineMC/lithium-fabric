package me.jellysquid.mods.lithium.mixin.alloc.world_ticking;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    @Shadow
    @Final
    private Int2ObjectMap<Entity> entitiesById;

    @Redirect(method = "tick",
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/entity/boss/dragon/EnderDragonFight;tick()V"),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;loadEntityUnchecked(Lnet/minecraft/entity/Entity;)V")
            ),
            at = @At(
                    remap = false,
                    value = "INVOKE",
                    target = "Lit/unimi/dsi/fastutil/objects/ObjectSet;iterator()Lit/unimi/dsi/fastutil/objects/ObjectIterator;"
            ))
    private ObjectIterator<Int2ObjectMap.Entry<Entity>> iterator(ObjectSet<Int2ObjectMap.Entry<Entity>> set) {
        // Avoids allocating a new Map entry object for every iterated value
        return Int2ObjectMaps.fastIterator(this.entitiesById);
    }
}
