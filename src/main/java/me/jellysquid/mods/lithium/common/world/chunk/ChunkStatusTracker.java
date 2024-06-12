package me.jellysquid.mods.lithium.common.world.chunk;

import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.function.BiConsumer;

public class ChunkStatusTracker {

    //Add other callback types in the future when needed
    private static final ArrayList<BiConsumer<ServerWorld, ChunkPos>> UNLOAD_CALLBACKS = new ArrayList<>();
    public static void onChunkStatusChange(ServerWorld serverWorld, ChunkPos pos, ChunkLevelType levelType) {
        boolean loaded = levelType.isAfter(ChunkLevelType.FULL);
        if (!loaded) {
            for (int i = 0; i < UNLOAD_CALLBACKS.size(); i++) {
                UNLOAD_CALLBACKS.get(i).accept(serverWorld, pos);
            }
        }
    }

    public static void registerUnloadCallback(BiConsumer<ServerWorld, ChunkPos> callback) {
        UNLOAD_CALLBACKS.add(callback);
    }
}
