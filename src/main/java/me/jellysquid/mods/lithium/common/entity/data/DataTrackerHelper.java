package me.jellysquid.mods.lithium.common.entity.data;

import io.netty.handler.codec.EncoderException;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.util.PacketByteBuf;

import java.util.Iterator;

public class DataTrackerHelper {
    // [VanillaCopy] DataTracker#toPacketByteBuf(PacketByteBuf)
    public static void toPacketByteBuf(PacketByteBuf buf, Iterator<DataTracker.Entry<?>> entries) {
        while (entries.hasNext()) {
            writeEntryToPacket(buf, entries.next());
        }

        buf.writeByte(255);
    }

    // [VanillaCopy] DataTracker#writeEntryToPacket<T>(PacketByteBuf, DataTracker.Entry<T>)
    public static <T> void writeEntryToPacket(PacketByteBuf buf, DataTracker.Entry<T> entry) {
        TrackedData<T> data = entry.getData();

        int id = TrackedDataHandlerRegistry.getId(data.getType());

        if (id < 0) {
            throw new EncoderException("Unknown serializer type " + data.getType());
        } else {
            buf.writeByte(data.getId());
            buf.writeVarInt(id);

            data.getType().write(buf, entry.get());
        }
    }
}
