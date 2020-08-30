package me.jellysquid.mods.lithium.mixin.world.player_chunk_tick;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.PlayerChunkWatchingManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class MixinThreadedAnvilChunkStorage {
    /**
     * @author JellySquid
     * @reason Defer sending chunks to the player so that we can batch them together
     */
    @Overwrite
    public void updateCameraPosition(ServerPlayerEntity player) {
        for (ThreadedAnvilChunkStorage.EntityTracker tracker : this.entityTrackers.values()) {
            if (tracker.entity == player) {
                tracker.updateCameraPosition(this.world.getPlayers());
            } else {
                tracker.updateCameraPosition(player);
            }
        }

        ChunkSectionPos prevPos = player.getCameraPosition();
        ChunkSectionPos newPos = ChunkSectionPos.from(player);

        long prevPosKey = ChunkPos.toLong(prevPos.getX(), prevPos.getZ());
        long newPosKey = ChunkPos.toLong(newPos.getX(), newPos.getZ());

        // Check whether the player has entered a new chunk. If not, we do not need to consider
        if (prevPosKey == newPosKey) {
            return;
        }

        boolean isWatchDisabled = this.playerChunkWatchingManager.isWatchDisabled(player);
        boolean doesNotGenerateChunks = this.doesNotGenerateChunks(player);

        if (!isWatchDisabled) {
            this.ticketManager.handleChunkLeave(prevPos, player);
        }

        if (!doesNotGenerateChunks) {
            this.ticketManager.handleChunkEnter(newPos, player);
        }

        if (!isWatchDisabled && doesNotGenerateChunks) {
            this.playerChunkWatchingManager.disableWatch(player);
        }

        if (isWatchDisabled && !doesNotGenerateChunks) {
            this.playerChunkWatchingManager.enableWatch(player);
        }

        this.playerChunkWatchingManager.movePlayer(prevPosKey, newPosKey, player);

        // Notify the client that the chunk map origin has changed. This must happen before any chunk payloads are sent.
        this.method_20726(player);

        // We can only send chunks if the world matches. This hoists a check that would otherwise be performed every
        // time we try to send a chunk over.
        if (player.world == this.world) {
            this.sendChunks(player);
        }
    }

    private void sendChunks(ServerPlayerEntity player) {
        int centerX = MathHelper.floor(player.getX()) >> 4;
        int centerZ = MathHelper.floor(player.getZ()) >> 4;

        int prevX = player.getCameraPosition().getSectionX();
        int prevZ = player.getCameraPosition().getSectionZ();

        int distance = this.watchDistance;
        int diameter = distance * 2;

        if (Math.abs(prevX - centerX) <= diameter && Math.abs(prevZ - centerZ) <= diameter) {
            int minX = Math.min(centerX, prevX) - distance;
            int minZ = Math.min(centerZ, prevZ) - distance;
            int maxX = Math.max(centerX, prevX) + distance;
            int maxZ = Math.max(centerZ, prevZ) + distance;

            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    boolean prevLoaded = getChunkDistance(x, z, prevX, prevZ) <= distance;
                    boolean loaded = getChunkDistance(x, z, centerX, centerZ) <= distance;

                    if (loaded && !prevLoaded) {
                        this.startWatchingChunk(player, x, z);
                    }

                    if (prevLoaded && !loaded) {
                        this.stopWatchingChunk(player, x, z);
                    }
                }
            }
        } else {
            for (int x = prevX - distance; x <= prevX + distance; ++x) {
                for (int z = prevZ - distance; z <= prevZ + distance; ++z) {
                    this.stopWatchingChunk(player, x, z);
                }
            }

            for (int x = centerX - distance; x <= centerX + distance; ++x) {
                for (int z = centerZ - distance; z <= centerZ + distance; ++z) {
                    this.startWatchingChunk(player, x, z);
                }
            }
        }
    }

    protected void startWatchingChunk(ServerPlayerEntity player, int x, int z) {
        ChunkHolder holder = this.getChunkHolder(ChunkPos.toLong(x, z));

        if (holder != null) {
            WorldChunk chunk = holder.getWorldChunk();

            if (chunk != null) {
                this.sendChunkDataPackets(player, new Packet[2], chunk);
            }
        }
    }

    protected void stopWatchingChunk(ServerPlayerEntity player, int x, int z) {
        player.sendUnloadChunkPacket(new ChunkPos(x, z));
    }

    private static int getChunkDistance(int x, int z, int centerX, int centerZ) {
        return Math.max(Math.abs(x - centerX), Math.abs(z - centerZ));
    }

    @Shadow
    @Final
    private Int2ObjectMap<ThreadedAnvilChunkStorage.EntityTracker> entityTrackers;

    @Shadow
    @Final
    private ServerWorld world;

    @Shadow
    @Final
    private PlayerChunkWatchingManager playerChunkWatchingManager;

    @Shadow
    @Final
    private ThreadedAnvilChunkStorage.TicketManager ticketManager;

    @Shadow
    private int watchDistance;

    @Shadow
    protected abstract boolean doesNotGenerateChunks(ServerPlayerEntity player);

    @Shadow
    protected abstract ChunkSectionPos method_20726(ServerPlayerEntity serverPlayerEntity);

    @Shadow
    protected abstract ChunkHolder getChunkHolder(long pos);

    @Shadow
    protected abstract void sendChunkDataPackets(ServerPlayerEntity player, Packet<?>[] packets, WorldChunk chunk);
}
