package net.caffeinemc.mods.lithium.mixin.alloc.entity_tracker;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.network.ServerPlayerConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;

@Mixin(ChunkMap.TrackedEntity.class)
public class ChunkMap$TrackedEntityMixin {

    /**
     * Uses less memory, and will cache the returned iterator.
     */
    @Redirect(
            method = "<init>",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/google/common/collect/Sets;newIdentityHashSet()Ljava/util/Set;",
                    remap = false
            )
    )
    private Set<ServerPlayerConnection> useFasterCollection() {
        return new ReferenceOpenHashSet<>();
    }
}
