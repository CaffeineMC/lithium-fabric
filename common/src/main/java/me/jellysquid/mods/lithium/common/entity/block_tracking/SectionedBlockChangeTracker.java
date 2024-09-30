package me.jellysquid.mods.lithium.common.entity.block_tracking;

import me.jellysquid.mods.lithium.common.block.BlockListeningSection;
import me.jellysquid.mods.lithium.common.block.ListeningBlockStatePredicate;
import me.jellysquid.mods.lithium.common.util.Pos;
import me.jellysquid.mods.lithium.common.util.deduplication.LithiumInterner;
import me.jellysquid.mods.lithium.common.util.tuples.WorldSectionBox;
import me.jellysquid.mods.lithium.common.world.LithiumData;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import java.util.ArrayList;
import java.util.Objects;

public class SectionedBlockChangeTracker {
    public final WorldSectionBox trackedWorldSections;
    public final ListeningBlockStatePredicate blockGroup;

    private long maxChangeTime;

    private int timesRegistered;
    //Some sections may not exist / be unloaded. We have to be aware of those. //TODO Invalidation when sections / chunks unload (but the entity does not (?), not sure whether this is possible) -> might be possible with entities riding lazy loaded vehicles and pushed to different chunks
    boolean isListeningToAll = false;
    private ArrayList<SectionPos> sectionsNotListeningTo = null;
    private ArrayList<BlockListeningSection> sectionsUnsubscribed = null;

    public SectionedBlockChangeTracker(WorldSectionBox trackedWorldSections, ListeningBlockStatePredicate blockGroup) {
        this.trackedWorldSections = trackedWorldSections;
        this.blockGroup = blockGroup;

        this.maxChangeTime = 0;
    }

    public boolean matchesMovedBox(AABB box) {
        return this.trackedWorldSections.matchesRelevantBlocksBox(box);
    }

    public static SectionedBlockChangeTracker registerAt(Level world, AABB entityBoundingBox, ListeningBlockStatePredicate blockGroup) {
        WorldSectionBox worldSectionBox = WorldSectionBox.relevantExpandedBlocksBox(world, entityBoundingBox);
        SectionedBlockChangeTracker tracker = new SectionedBlockChangeTracker(worldSectionBox, blockGroup);

        LithiumInterner<SectionedBlockChangeTracker> blockChangeTrackers = ((LithiumData) world).lithium$getData().blockChangeTrackers();
        tracker = blockChangeTrackers.getCanonical(tracker);

        tracker.register();
        return tracker;
    }

    long getWorldTime() {
        return this.trackedWorldSections.world().getGameTime();
    }

    public void register() {
        if (this.timesRegistered == 0) {
            WorldSectionBox trackedSections = this.trackedWorldSections;
            for (int x = trackedSections.chunkX1(); x < trackedSections.chunkX2(); x++) {
                for (int z = trackedSections.chunkZ1(); z < trackedSections.chunkZ2(); z++) {
                    Level world = trackedSections.world();
                    ChunkAccess chunk = world.getChunk(x, z, ChunkStatus.FULL, false);
                    LevelChunkSection[] sectionArray = chunk == null ? null : chunk.getSections();
                    for (int y = trackedSections.chunkY1(); y < trackedSections.chunkY2(); y++) {
                        if (Pos.SectionYCoord.getMinYSection(world) > y || Pos.SectionYCoord.getMaxYSectionExclusive(world) <= y) {
                            continue;
                        }
                        SectionPos sectionPos = SectionPos.of(x, y, z);
                        if (sectionArray == null) {
                            if (this.sectionsNotListeningTo == null) {
                                this.sectionsNotListeningTo = new ArrayList<>();
                            }
                            this.sectionsNotListeningTo.add(sectionPos);
                            continue;
                        }
                        LevelChunkSection section = sectionArray[Pos.SectionYIndex.fromSectionCoord(world, y)];

                        BlockListeningSection blockListeningSection = (BlockListeningSection) section;
                        blockListeningSection.lithium$addToCallback(this.blockGroup, this, SectionPos.asLong(x, y, z), world);
                    }
                }
            }
            this.isListeningToAll = (this.sectionsNotListeningTo == null || this.sectionsNotListeningTo.isEmpty())
                    && (this.sectionsUnsubscribed == null || this.sectionsUnsubscribed.isEmpty());
            this.setChanged(this.getWorldTime());
        }
        this.timesRegistered++;
    }

    public void unregister() {
        if (--this.timesRegistered > 0) {
            return;
        }
        WorldSectionBox trackedSections = this.trackedWorldSections;
        Level world = trackedSections.world();
        for (int x = trackedSections.chunkX1(); x < trackedSections.chunkX2(); x++) {
            for (int z = trackedSections.chunkZ1(); z < trackedSections.chunkZ2(); z++) {
                ChunkAccess chunk = world.getChunk(x, z, ChunkStatus.FULL, false);
                LevelChunkSection[] sectionArray = chunk == null ? null : chunk.getSections();
                for (int y = trackedSections.chunkY1(); y < trackedSections.chunkY2(); y++) {

                    if (sectionArray == null) {
                        continue;
                    }
                    if (Pos.SectionYCoord.getMinYSection(world) > y || Pos.SectionYCoord.getMaxYSectionExclusive(world) <= y) {
                        continue;
                    }
                    LevelChunkSection section = sectionArray[Pos.SectionYIndex.fromSectionCoord(world, y)];

                    BlockListeningSection blockListeningSection = (BlockListeningSection) section;
                    blockListeningSection.lithium$removeFromCallback(this.blockGroup, this);
                }
            }
        }
        this.sectionsNotListeningTo = null;
        LithiumInterner<SectionedBlockChangeTracker> blockChangeTrackers = ((LithiumData) world).lithium$getData().blockChangeTrackers();
        blockChangeTrackers.deleteCanonical(this);
    }

    public void listenToAllSections() {
        boolean changed = false;
        ArrayList<SectionPos> notListeningTo = this.sectionsNotListeningTo;
        if (notListeningTo != null) {
            for (int i = notListeningTo.size() - 1; i >= 0; i--) {
                changed = true;
                SectionPos chunkSectionPos = notListeningTo.get(i);
                Level world = this.trackedWorldSections.world();
                ChunkAccess chunk = world.getChunk(chunkSectionPos.getX(), chunkSectionPos.getZ(), ChunkStatus.FULL, false);
                if (chunk != null) {
                    notListeningTo.remove(i);
                } else {
                    //Chunk not loaded, cannot listen to all sections.
                    return;
                }
                LevelChunkSection section = chunk.getSections()[Pos.SectionYIndex.fromSectionCoord(world, chunkSectionPos.getY())];
                BlockListeningSection blockListeningSection = (BlockListeningSection) section;
                blockListeningSection.lithium$addToCallback(this.blockGroup, this, chunkSectionPos.asLong(), world);
            }
        }
        if (this.sectionsUnsubscribed != null) {
            ArrayList<BlockListeningSection> unsubscribed = this.sectionsUnsubscribed;
            for (int i = unsubscribed.size() - 1; i >= 0; i--) {
                changed = true;
                BlockListeningSection blockListeningSection = unsubscribed.remove(i);
                blockListeningSection.lithium$addToCallback(this.blockGroup, this, Long.MIN_VALUE, null);
            }
        }
        this.isListeningToAll = true;
        if (changed) {
            this.setChanged(this.getWorldTime());
        }
    }

    public void setChanged(BlockListeningSection section) {
        if (this.sectionsUnsubscribed == null) {
            this.sectionsUnsubscribed = new ArrayList<>();
        }
        this.sectionsUnsubscribed.add(section);
        this.setChanged(this.getWorldTime());
        this.isListeningToAll = false;
    }

    public void setChanged(long atTime) {
        if (atTime > this.maxChangeTime) {
            this.maxChangeTime = atTime;
        }
    }

    /**
     * Method to quickly check whether any relevant blocks changed inside the relevant chunk sections after
     * the last test.
     *
     * @param lastCheckedTime time of the last interaction attempt
     * @return whether any relevant entity moved in the tracked area
     */
    public boolean isUnchangedSince(long lastCheckedTime) {
        if (lastCheckedTime <= this.maxChangeTime) {
            return false;
        }
        if (!this.isListeningToAll) {
            this.listenToAllSections();
            return this.isListeningToAll && lastCheckedTime > this.maxChangeTime;
        }
        return true;
    }

    //Do not modify, used for deduplication of instances
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SectionedBlockChangeTracker) obj;
        return Objects.equals(this.trackedWorldSections, that.trackedWorldSections) &&
                Objects.equals(this.blockGroup, that.blockGroup);
    }
    //Do not modify, used for deduplication of instances
    @Override
    public int hashCode() {
        return this.getClass().hashCode() ^ this.trackedWorldSections.hashCode() ^ this.blockGroup.hashCode();
    }

    public void onChunkSectionInvalidated(SectionPos sectionPos) {
        if (this.sectionsNotListeningTo == null) {
            this.sectionsNotListeningTo = new ArrayList<>();
        }
        this.sectionsNotListeningTo.add(sectionPos);
        this.setChanged(this.getWorldTime());
        this.isListeningToAll = false;
    }
}
