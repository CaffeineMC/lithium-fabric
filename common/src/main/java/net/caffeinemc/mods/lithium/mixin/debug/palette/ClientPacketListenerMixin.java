package net.caffeinemc.mods.lithium.mixin.debug.palette;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Arrays;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @WrapOperation(
            method = "handleLevelChunkWithLight", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientChunkCache;replaceWithPacketData(IILnet/minecraft/network/protocol/game/ClientboundLevelChunkPacketData;)Lnet/minecraft/world/level/chunk/LevelChunk;")
    )
    private LevelChunk addExceptionInfo(ClientChunkCache instance, int chunkX, int chunkZ, ClientboundLevelChunkPacketData chunkData, Operation<LevelChunk> original) {
        try {
            original.call(instance, chunkX, chunkZ, chunkData);
        } catch (IllegalStateException e) {
            String message = "Exception occurred while receiving data for chunk at " + chunkX + ", " + chunkZ + ".\n" +
                    "**The following may include sensitive data, e.g. text that is written with blocks or built \n" +
                    "structures. Make sure the chunk with chunk coordinates " + chunkX + ", " + chunkZ + " does not contain block\n" +
                    "or biome structures (e.g. your non-pseudonym name written with blocks) that you do not want\n" +
                    "published. This does not include block entities or items.**\n" +
                    "Possible sensitive chunk biome and blockstate data: " + Arrays.toString(((ClientBoundLevelChunkPacketDataAccessor) chunkData).getBuffer());
            throw new IllegalStateException(message, e);
        }
        return null;
    }
}
