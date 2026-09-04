package net.caffeinemc.mods.lithium.mixin.minimal_nonvanilla.spawning;

import net.caffeinemc.mods.lithium.common.world.ChunkAwareEntityIterable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.NaturalSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NaturalSpawner.class)
public class NaturalSpawnerMixin {

    @Redirect(
            method = "createState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;getAllEntities()Ljava/lang/Iterable;"
            )
    )
    private static Iterable<Entity> iterateEntitiesChunkAware(ServerLevel serverWorld) {
        //noinspection unchecked
        return ((ChunkAwareEntityIterable<Entity>) ((PersistentEntitySectionManagerAccessor<Entity>) ((ServerLevelAccessor) serverWorld).getEntityManager()).getCache()).lithium$IterateEntitiesInTrackedSections();
    }
}
