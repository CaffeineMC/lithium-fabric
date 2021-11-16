package me.jellysquid.mods.lithium.mixin.entity.fast_retrieval;

import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.entity.EntityTrackingSection;
import net.minecraft.world.entity.SectionedEntityCache;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Consumer;

@Mixin(SectionedEntityCache.class)
public abstract class SectionedEntityCacheMixin<T extends EntityLike> {
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


        // Vanilla order of the AVL long set is sorting by ascending long value. The x, y, z positions are packed into
        // a long with the x position's lowest 22 bits placed at the MSB.
        // Therefore the long is negative iff the 22th bit of the x position is set, which happens iff the x position
        // is negative. A positive x position will never have its 22th bit set, as these big coordinates are far outside
        // the world. y and z positions are treated as unsigned when sorting by ascending long value, as their sign bits
        // are placed somewhere inside the packed long

        for (int x = minX; x <= maxX; x++) {
            for (int z = Math.max(minZ, 0); z <= maxZ; z++) {
                this.forEachInColumn(x, minY, maxY, z, action);
            }

            int bound = Math.min(-1, maxZ);
            for (int z = minZ; z <= bound; z++) {
                this.forEachInColumn(x, minY, maxY, z, action);
            }
        }
    }

    private void forEachInColumn(int x, int minY, int maxY, int z, Consumer<EntityTrackingSection<T>> action) {
        //y from negative to positive, but y is treated as unsigned
        for (int y = Math.max(minY, 0); y <= maxY; y++) {
            this.consumeSection(x, y, z, action);
        }
        int bound = Math.min(-1, maxY);
        for (int y = minY; y <= bound; y++) {
            this.consumeSection(x, y, z, action);
        }
    }

    private void consumeSection(int x, int y, int z, Consumer<EntityTrackingSection<T>> action) {
        EntityTrackingSection<T> section = this.findTrackingSection(ChunkSectionPos.asLong(x, y, z));
        if (section != null && section.getStatus().shouldTrack()) {
            action.accept(section);
        }
    }
}
