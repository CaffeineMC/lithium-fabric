package me.jellysquid.mods.lithium.common.block;

import me.jellysquid.mods.lithium.common.entity.block_tracking.SectionedBlockChangeTracker;
import net.minecraft.util.math.ChunkSectionPos;

public interface BlockListeningSection {

    void lithium$addToCallback(ListeningBlockStatePredicate blockGroup, SectionedBlockChangeTracker tracker);

    void lithium$removeFromCallback(ListeningBlockStatePredicate blockGroup, SectionedBlockChangeTracker tracker);

    void lithium$invalidateListeningSection(ChunkSectionPos sectionPos);
}
