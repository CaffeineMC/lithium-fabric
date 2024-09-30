package me.jellysquid.mods.lithium.common.block;

import me.jellysquid.mods.lithium.common.entity.block_tracking.SectionedBlockChangeTracker;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;

public interface BlockListeningSection {

    void lithium$addToCallback(ListeningBlockStatePredicate blockGroup, SectionedBlockChangeTracker tracker, long sectionPos, Level world);

    void lithium$removeFromCallback(ListeningBlockStatePredicate blockGroup, SectionedBlockChangeTracker tracker);

    void lithium$invalidateListeningSection(SectionPos sectionPos);
}
