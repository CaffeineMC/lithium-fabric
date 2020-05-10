package me.jellysquid.mods.lithium.mixin.world.player_chunk_tick;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket;
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

import java.util.Arrays;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class MixinThreadedAnvilChunkStorage {
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

    /**
     * @reason Avoid allocations
     * @author JellySquid
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

        int centerX = MathHelper.floor(player.getX()) >> 4;
        int centerZ = MathHelper.floor(player.getZ()) >> 4;

        ChunkSectionPos cameraPos = player.getCameraPosition();
        ChunkSectionPos entityPos = ChunkSectionPos.from(player);

        long cameraPosL = cameraPos.toChunkPos().toLong();
        long entityPosL = entityPos.toChunkPos().toLong();

        boolean isWatchDisabled = this.playerChunkWatchingManager.isWatchDisabled(player);
        boolean isGeneratingDisabled = this.doesNotGenerateChunks(player);
        boolean isPlayerMoving = cameraPosL != entityPosL;

        if (isPlayerMoving || isWatchDisabled != isGeneratingDisabled) {
            this.method_20726(player);

            if (!isWatchDisabled) {
                this.ticketManager.handleChunkLeave(cameraPos, player);
            }

            if (!isGeneratingDisabled) {
                this.ticketManager.handleChunkEnter(entityPos, player);
            }

            if (!isWatchDisabled && isGeneratingDisabled) {
                this.playerChunkWatchingManager.disableWatch(player);
            }

            if (isWatchDisabled && !isGeneratingDisabled) {
                this.playerChunkWatchingManager.enableWatch(player);
            }

            if (cameraPosL != entityPosL) {
                this.playerChunkWatchingManager.movePlayer(cameraPosL, entityPosL, player);
            }
        }

        if (player.world != this.world) {
            return;
        }

        int cameraCenterX = cameraPos.getSectionX();
        int cameraCenterZ = cameraPos.getSectionZ();

        // TODO: Only iterate over the differing areas
        if (Math.abs(cameraCenterX - centerX) <= this.watchDistance * 2 && Math.abs(cameraCenterZ - centerZ) <= this.watchDistance * 2) {
            int minChunkX = Math.min(centerX, cameraCenterX) - this.watchDistance;
            int minChunkZ = Math.min(centerZ, cameraCenterZ) - this.watchDistance;

            int maxChunkX = Math.max(centerX, cameraCenterX) + this.watchDistance;
            int maxChunkZ = Math.max(centerZ, cameraCenterZ) + this.watchDistance;

            for (int chunkX = minChunkX; chunkX <= maxChunkX; ++chunkX) {
                for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; ++chunkZ) {
                    boolean withinMaxWatchDistance = getChebyshevDistance(chunkX, chunkZ, cameraCenterX, cameraCenterZ) <= this.watchDistance;
                    boolean withinViewDistance = getChebyshevDistance(chunkX, chunkZ, centerX, centerZ) <= this.watchDistance;

                    this.sendWatchPackets$lithium(player, chunkX, chunkZ, this.getCachedWatchPacketsArray(), withinMaxWatchDistance, withinViewDistance);
                }
            }
        } else {
            for (int chunkX = cameraCenterX - this.watchDistance; chunkX <= cameraCenterX + this.watchDistance; ++chunkX) {
                for (int chunkZ = cameraCenterZ - this.watchDistance; chunkZ <= cameraCenterZ + this.watchDistance; ++chunkZ) {
                    this.sendWatchPackets$lithium(player, chunkX, chunkZ, this.getCachedWatchPacketsArray(), true, false);
                }
            }

            for (int chunkX = centerX - this.watchDistance; chunkX <= centerX + this.watchDistance; ++chunkX) {
                for (int chunkZ = centerZ - this.watchDistance; chunkZ <= centerZ + this.watchDistance; ++chunkZ) {
                    this.sendWatchPackets$lithium(player, chunkX, chunkZ, this.getCachedWatchPacketsArray(), false, true);
                }
            }
        }

        // Prevent a memory leak by clearing packets in the array
        Arrays.fill(this.cachedWatchPackets, null);
    }

    // [VanillaCopy] ThreadedAnvilChunkStorage#sendWatchPackets
    // Avoids allocating ChunkPos where possible
    private void sendWatchPackets$lithium(ServerPlayerEntity player, int x, int z, Packet<?>[] packets, boolean withinMaxWatchDistance, boolean withinViewDistance) {
        if (withinViewDistance && !withinMaxWatchDistance) {
            // Encode the chunk position directly
            ChunkHolder holder = this.getChunkHolder(ChunkPos.toLong(x, z));

            if (holder != null) {
                WorldChunk chunk = holder.getWorldChunk();

                if (chunk != null) {
                    this.sendChunkDataPackets(player, packets, chunk);
                }
            }
        }

        // Bring down the method to send an unload packet to the player so we can directly pass integer coordinates
        // without allocating.
        if ((!withinViewDistance && withinMaxWatchDistance) && player.isAlive()) {
            player.networkHandler.sendPacket(new UnloadChunkS2CPacket(x, z));
        }
    }

    private final Packet<?>[] cachedWatchPackets = new Packet[2];

    /**
     * Returns a prepared packet array for use with {@link ThreadedAnvilChunkStorage#sendWatchPackets(ServerPlayerEntity, ChunkPos, Packet[], boolean, boolean)}.
     */
    private Packet<?>[] getCachedWatchPacketsArray() {
        Arrays.fill(this.cachedWatchPackets, null);

        return this.cachedWatchPackets;
    }

    private static int getChebyshevDistance(int originX, int originZ, int x, int z) {
        return Math.max(Math.abs(originX - x), Math.abs(originZ - z));
    }
}
