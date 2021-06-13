package me.jellysquid.mods.lithium.mixin.entity.fast_retrieval;

import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.entity.EntityTrackingSection;
import net.minecraft.world.entity.SectionedEntityCache;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Consumer;

@Mixin(SectionedEntityCache.class)
public abstract class SectionedEntityCacheMixin<T> {
    @Shadow
    @Nullable
    public abstract EntityTrackingSection<T> findTrackingSection(long sectionPos);

    /**
     * @author 2No2Name
     * @reason avoid iterating through LongAVLTreeSet, possibly iterating over hundreds of irrelevant longs to save up to 8 hash set gets
     */
    @Overwrite
    public void forEachInBox(Box box, Consumer<EntityTrackingSection<T>> action) {
        int minX = ChunkSectionPos.getSectionCoord(box.minX - 2.0D);
        int minY = ChunkSectionPos.getSectionCoord(box.minY - 2.0D);
        int minZ = ChunkSectionPos.getSectionCoord(box.minZ - 2.0D);
        int maxX = ChunkSectionPos.getSectionCoord(box.maxX + 2.0D);
        int maxY = ChunkSectionPos.getSectionCoord(box.maxY + 2.0D);
        int maxZ = ChunkSectionPos.getSectionCoord(box.maxZ + 2.0D);

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    EntityTrackingSection<T> section = this.findTrackingSection(ChunkSectionPos.asLong(x, y, z));
                    if (section != null && section.getStatus().shouldTrack()) {
                        action.accept(section);
                    }
                }
            }
        }
    }
}
