package me.jellysquid.mods.lithium.common.block;

import me.jellysquid.mods.lithium.common.entity.block_tracking.SectionedBlockChangeTracker;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;

public interface BlockListeningSection {

    void lithium$addToCallback(ListeningBlockStatePredicate blockGroup, SectionedBlockChangeTracker tracker, long sectionPos, World world);

    void lithium$removeFromCallback(ListeningBlockStatePredicate blockGroup, SectionedBlockChangeTracker tracker);

    void lithium$invalidateListeningSection(ChunkSectionPos sectionPos);
}
