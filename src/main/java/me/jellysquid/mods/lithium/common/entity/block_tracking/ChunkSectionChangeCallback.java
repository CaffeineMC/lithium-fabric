package me.jellysquid.mods.lithium.common.entity.block_tracking;

import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import me.jellysquid.mods.lithium.common.block.BlockListeningSection;
import me.jellysquid.mods.lithium.common.block.BlockStateFlags;
import me.jellysquid.mods.lithium.common.block.ListeningBlockStatePredicate;
import me.jellysquid.mods.lithium.common.util.Pos;
import me.jellysquid.mods.lithium.common.world.LithiumData;
import me.jellysquid.mods.lithium.common.world.chunk.ChunkStatusTracker;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;

import java.util.ArrayList;

public final class ChunkSectionChangeCallback {
    private final ArrayList<SectionedBlockChangeTracker>[] trackers;
    private short listeningMask;

    static {
        if (BlockListeningSection.class.isAssignableFrom(ChunkSection.class)) {
            ChunkStatusTracker.registerUnloadCallback((serverWorld, chunkPos) -> {
                Long2ReferenceOpenHashMap<ChunkSectionChangeCallback> changeCallbacks = ((LithiumData) serverWorld).lithium$getData().chunkSectionChangeCallbacks();
                int x = chunkPos.x;
                int z = chunkPos.z;
                for (int y = Pos.SectionYCoord.getMinYSection(serverWorld); y <= Pos.SectionYCoord.getMaxYSectionInclusive(serverWorld); y++) {
                    ChunkSectionPos chunkSectionPos = ChunkSectionPos.from(x, y, z);
                    ChunkSectionChangeCallback chunkSectionChangeCallback = changeCallbacks.remove(chunkSectionPos.asLong());
                    if (chunkSectionChangeCallback != null) {
                        chunkSectionChangeCallback.onChunkSectionInvalidated(chunkSectionPos);
                    }
                }
            });
        }
    }

    public ChunkSectionChangeCallback() {
        //noinspection unchecked
        this.trackers = new ArrayList[BlockStateFlags.NUM_LISTENING_FLAGS];
        this.listeningMask = 0;
    }

    public static ChunkSectionChangeCallback create(long sectionPos, World world) {
        ChunkSectionChangeCallback chunkSectionChangeCallback = new ChunkSectionChangeCallback();
        Long2ReferenceOpenHashMap<ChunkSectionChangeCallback> changeCallbacks = ((LithiumData) world).lithium$getData().chunkSectionChangeCallbacks();
        ChunkSectionChangeCallback previous = changeCallbacks.put(sectionPos, chunkSectionChangeCallback);
        if (previous != null) {
            previous.onChunkSectionInvalidated(ChunkSectionPos.from(sectionPos));
        }
        return chunkSectionChangeCallback;
    }

    public short onBlockChange(int blockGroupIndex, BlockListeningSection section) {
        ArrayList<SectionedBlockChangeTracker> sectionedBlockChangeTrackers = this.trackers[blockGroupIndex];
        this.trackers[blockGroupIndex] = null;
        if (sectionedBlockChangeTrackers != null) {
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < sectionedBlockChangeTrackers.size(); i++) {
                sectionedBlockChangeTrackers.get(i).setChanged(section);
            }
        }
        this.listeningMask &= (short) ~(1 << blockGroupIndex);

        return this.listeningMask;
    }

    public short addTracker(SectionedBlockChangeTracker tracker, ListeningBlockStatePredicate blockGroup) {
        int blockGroupIndex = blockGroup.getIndex();
        ArrayList<SectionedBlockChangeTracker> sectionedBlockChangeTrackers = this.trackers[blockGroupIndex];
        if (sectionedBlockChangeTrackers == null) {
            this.trackers[blockGroupIndex] = (sectionedBlockChangeTrackers = new ArrayList<>());
        }
        sectionedBlockChangeTrackers.add(tracker);

        this.listeningMask |= (short) (1 << blockGroupIndex);
        return this.listeningMask;
    }

    public short removeTracker(SectionedBlockChangeTracker tracker, ListeningBlockStatePredicate blockGroup) {
        int blockGroupIndex = blockGroup.getIndex();
        ArrayList<SectionedBlockChangeTracker> sectionedBlockChangeTrackers = this.trackers[blockGroupIndex];
        if (sectionedBlockChangeTrackers != null) {
            sectionedBlockChangeTrackers.remove(tracker);
            if (sectionedBlockChangeTrackers.isEmpty()) {
                this.listeningMask &= (short) ~(1 << blockGroup.getIndex());
            }
        }
        return this.listeningMask;
    }

    public void onChunkSectionInvalidated(ChunkSectionPos sectionPos) {
        for (int flagIndex = 0; flagIndex < this.trackers.length; flagIndex++) {
            ArrayList<SectionedBlockChangeTracker> sectionedBlockChangeTrackers = this.trackers[flagIndex];
            this.trackers[flagIndex] = null;
            if (sectionedBlockChangeTrackers != null) {
                //noinspection ForLoopReplaceableByForEach
                for (int i = 0; i < sectionedBlockChangeTrackers.size(); i++) {
                    sectionedBlockChangeTrackers.get(i).onChunkSectionInvalidated(sectionPos);
                }
            }
        }
        this.listeningMask = 0;
    }
}
