package me.jellysquid.mods.lithium.mixin.world.player_chunk_tick;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import me.jellysquid.mods.lithium.common.util.Pos;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.PlayerChunkWatchingManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.WorldChunk;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import static net.minecraft.server.world.ThreadedAnvilChunkStorage.isWithinDistance;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class ThreadedAnvilChunkStorageMixin {
    @Shadow
    protected abstract void sendChunkDataPackets(ServerPlayerEntity player, MutableObject<ChunkDataS2CPacket> cachedDataPacket, WorldChunk chunk);

    @Shadow
    protected abstract ChunkSectionPos updateWatchedSection(ServerPlayerEntity player);

    /**
     * @author JellySquid
     * @reason Defer sending chunks to the player so that we can batch them together
     */
    @Overwrite
    public void updatePosition(ServerPlayerEntity player) {
        for (ThreadedAnvilChunkStorage.EntityTracker tracker : this.entityTrackers.values()) {
            if (tracker.entity == player) {
                tracker.updateTrackedStatus(this.world.getPlayers());
            } else {
                tracker.updateTrackedStatus(player);
            }
        }

        ChunkSectionPos oldPos = player.getWatchedSection();
        ChunkSectionPos newPos = ChunkSectionPos.from(player);

        boolean isWatchingWorld = this.playerChunkWatchingManager.isWatchDisabled(player);
        boolean doesNotGenerateChunks = this.doesNotGenerateChunks(player);
        boolean movedSections = !newPos.equals(oldPos);

        if (movedSections || isWatchingWorld != doesNotGenerateChunks) {
            // Notify the client that the chunk map origin has changed. This must happen before any chunk payloads are sent.
            this.updateWatchedSection(player);

            if (!isWatchingWorld) {
                this.ticketManager.handleChunkLeave(oldPos, player);
            }

            if (!doesNotGenerateChunks) {
                this.ticketManager.handleChunkEnter(newPos, player);
            }

            if (!isWatchingWorld && doesNotGenerateChunks) {
                this.playerChunkWatchingManager.disableWatch(player);
            }

            if (isWatchingWorld && !doesNotGenerateChunks) {
                this.playerChunkWatchingManager.enableWatch(player);
            }

            long oldChunkPos = ChunkPos.toLong(oldPos.getX(), oldPos.getZ());
            long newChunkPos = ChunkPos.toLong(newPos.getX(), newPos.getZ());

            this.playerChunkWatchingManager.movePlayer(oldChunkPos, newChunkPos, player);
        } else {
            // The player hasn't changed locations and isn't changing dimensions
            return;
        }

        // We can only send chunks if the world matches. This hoists a check that
        // would otherwise be performed every time we try to send a chunk over.
        if (player.world == this.world) {
            this.sendChunks(oldPos, player);
        }
    }

    private void sendChunks(ChunkSectionPos oldPos, ServerPlayerEntity player) {
        int newCenterX = Pos.ChunkCoord.fromBlockCoord(MathHelper.floor(player.getX()));
        int newCenterZ = Pos.ChunkCoord.fromBlockCoord(MathHelper.floor(player.getZ()));

        int oldCenterX = oldPos.getSectionX();
        int oldCenterZ = oldPos.getSectionZ();

        int watchRadius = this.watchDistance;
        int watchRadiusIncr = watchRadius + 1;
        int watchDiameter = watchRadius * 2;

        if (Math.abs(oldCenterX - newCenterX) <= watchDiameter && Math.abs(oldCenterZ - newCenterZ) <= watchDiameter) {
            int minX = Math.min(newCenterX, oldCenterX) - watchRadiusIncr;
            int minZ = Math.min(newCenterZ, oldCenterZ) - watchRadiusIncr;
            int maxX = Math.max(newCenterX, oldCenterX) + watchRadiusIncr;
            int maxZ = Math.max(newCenterZ, oldCenterZ) + watchRadiusIncr;

            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    boolean isWithinOldRadius = isWithinDistance(x, z, oldCenterX, oldCenterZ, watchRadius);
                    boolean isWithinNewRadius = isWithinDistance(x, z, newCenterX, newCenterZ, watchRadius);

                    if (isWithinNewRadius && !isWithinOldRadius) {
                        this.startWatchingChunk(player, x, z);
                    }

                    if (isWithinOldRadius && !isWithinNewRadius) {
                        this.stopWatchingChunk(player, x, z);
                    }
                }
            }
        } else {
            for (int x = oldCenterX - watchRadiusIncr; x <= oldCenterX + watchRadiusIncr; ++x) {
                for (int z = oldCenterZ - watchRadiusIncr; z <= oldCenterZ + watchRadiusIncr; ++z) {
                    if (isWithinDistance(x, z, oldCenterX, oldCenterZ, watchRadius)) {
                        this.stopWatchingChunk(player, x, z);
                    }
                }
            }

            for (int x = newCenterX - watchRadiusIncr; x <= newCenterX + watchRadiusIncr; ++x) {
                for (int z = newCenterZ - watchRadiusIncr; z <= newCenterZ + watchRadiusIncr; ++z) {
                    if (isWithinDistance(x, z, newCenterX, newCenterZ, watchRadius)) {
                        this.startWatchingChunk(player, x, z);
                    }
                }
            }
        }
    }

    protected void startWatchingChunk(ServerPlayerEntity player, int x, int z) {
        ChunkHolder holder = this.getChunkHolder(ChunkPos.toLong(x, z));

        if (holder != null) {
            WorldChunk chunk = holder.getWorldChunk();

            if (chunk != null) {
                this.sendChunkDataPackets(player, new MutableObject<>(), chunk);
            }
        }
    }

    protected void stopWatchingChunk(ServerPlayerEntity player, int x, int z) {
        player.sendUnloadChunkPacket(new ChunkPos(x, z));
    }

    @Shadow
    @Final
    private Int2ObjectMap<ThreadedAnvilChunkStorage.EntityTracker> entityTrackers;

    @Shadow
    @Final
    ServerWorld world;

    @Shadow
    @Final
    private PlayerChunkWatchingManager playerChunkWatchingManager;

    @Shadow
    @Final
    private ThreadedAnvilChunkStorage.TicketManager ticketManager;

    @Shadow
    int watchDistance;

    @Shadow
    protected abstract boolean doesNotGenerateChunks(ServerPlayerEntity player);

    @Shadow
    protected abstract ChunkHolder getChunkHolder(long pos);
}
