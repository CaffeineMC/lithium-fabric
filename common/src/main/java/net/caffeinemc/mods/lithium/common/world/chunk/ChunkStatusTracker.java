package net.caffeinemc.mods.lithium.common.world.chunk;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

public class ChunkStatusTracker {

    //Add other callback types in the future when needed
    private static final ArrayList<BiConsumer<ServerLevel, ChunkPos>> UNLOAD_CALLBACKS = new ArrayList<>();
    public static void onChunkStatusChange(ServerLevel serverWorld, ChunkPos pos, FullChunkStatus levelType) {
        boolean loaded = levelType.isOrAfter(FullChunkStatus.FULL);
        if (!loaded) {
            for (int i = 0; i < UNLOAD_CALLBACKS.size(); i++) {
                UNLOAD_CALLBACKS.get(i).accept(serverWorld, pos);
            }
        }
    }

    public static void registerUnloadCallback(BiConsumer<ServerLevel, ChunkPos> callback) {
        UNLOAD_CALLBACKS.add(callback);
    }
}
