package me.jellysquid.mods.lithium.mixin.entity.data_tracker.no_locks.network;

import me.jellysquid.mods.lithium.common.entity.data.DataTrackerHelper;
import net.minecraft.client.network.packet.PlayerSpawnS2CPacket;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PlayerSpawnS2CPacket.class)
public class MixinPlayerSpawnS2CPacket {
    @Shadow
    private DataTracker dataTracker;

    @Shadow
    private List<DataTracker.Entry<?>> trackedValues;

    @Inject(method = "<init>(Lnet/minecraft/entity/player/PlayerEntity;)V", at = @At("RETURN"))
    private void copyDataTrackerEntries(PlayerEntity playerEntity, CallbackInfo ci) {
        this.trackedValues = this.dataTracker.getAllEntries();
    }

    /**
     * Use copies we made earlier to avoid off-thread access of the DataTracker. This allows us to remove all the
     */
    @Redirect(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/data/DataTracker;toPacketByteBuf(Lnet/minecraft/util/PacketByteBuf;)V"))
    private void redirectSerializeEntries(DataTracker dataTracker, PacketByteBuf packetByteBuf) {
        DataTrackerHelper.toPacketByteBuf(packetByteBuf, this.trackedValues.iterator());
    }

}
