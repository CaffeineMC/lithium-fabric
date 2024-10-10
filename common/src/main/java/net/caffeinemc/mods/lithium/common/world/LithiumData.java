package net.caffeinemc.mods.lithium.common.world;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.caffeinemc.mods.lithium.common.entity.block_tracking.ChunkSectionChangeCallback;
import net.caffeinemc.mods.lithium.common.entity.block_tracking.SectionedBlockChangeTracker;
import net.caffeinemc.mods.lithium.common.entity.movement_tracker.SectionedEntityMovementTracker;
import net.caffeinemc.mods.lithium.common.util.deduplication.LithiumInterner;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEventListenerRegistry;

public interface LithiumData {

    record Data(
            // Map of chunk position -> y section -> game event dispatcher
            // This should be faster than the chunk lookup, since there are usually a lot more chunks than
            // chunk with game event dispatchers (we only initialize them when non-empty set of listeners)
            // All Int2ObjectMap objects are also stored in a field of the corresponding WorldChunk.
            Long2ReferenceOpenHashMap<Int2ObjectMap<GameEventListenerRegistry>> gameEventDispatchersByChunk,

            // Cached ominous banner, must not be mutated.
            ItemStack ominousBanner,

            // Set of active mob navigations (active = have a path)
            ReferenceOpenHashSet<PathNavigation> activeNavigations,

            // Block change tracker deduplication
            LithiumInterner<SectionedBlockChangeTracker> blockChangeTrackers,

            // Entity movement tracker deduplication
            LithiumInterner<SectionedEntityMovementTracker<?, ?>> entityMovementTrackers,

            // Block ChunkSection listeners
            Long2ReferenceOpenHashMap<ChunkSectionChangeCallback> chunkSectionChangeCallbacks
    ) {
        public Data(Level world) {
            this(
                    new Long2ReferenceOpenHashMap<>(),
                    world.registryAccess().lookup(Registries.BANNER_PATTERN).map(Raid::getLeaderBannerInstance).orElse(null),
                    new ReferenceOpenHashSet<>(),
                    new LithiumInterner<>(),
                    new LithiumInterner<>(),
                    new Long2ReferenceOpenHashMap<>()
            );
        }
    }

    LithiumData.Data lithium$getData();
}
