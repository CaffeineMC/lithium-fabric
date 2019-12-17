package me.jellysquid.mods.lithium.mixin.nbt.fast_serialization.region;

import me.jellysquid.mods.lithium.common.nbt.io.NbtFastIo;
import me.jellysquid.mods.lithium.common.nbt.io.NbtOut;
import me.jellysquid.mods.lithium.common.nbt.io.bytes.NbtInByteBuffer;
import me.jellysquid.mods.lithium.common.nbt.io.bytes.NbtOutByteBuffer;
import me.jellysquid.mods.lithium.common.nbt.region.RegionFileNbtBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.RegionBasedStorage;
import net.minecraft.world.storage.RegionFile;
import org.apache.commons.io.IOUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

@SuppressWarnings("OverwriteModifiers")
@Mixin(RegionBasedStorage.class)
public abstract class MixinRegionBasedStorage {
    @Shadow
    protected abstract RegionFile getRegionFile(ChunkPos pos) throws IOException;

    /**
     * @author JellySquid
     */
    @Overwrite
    public CompoundTag getTagAt(ChunkPos pos) throws IOException {
        RegionFile file = this.getRegionFile(pos);

        InputStream data = file.getChunkDataInputStream(pos);

        if (data == null) {
            return null;
        }

        ByteBuffer buf;

        try (ByteArrayOutputStream out = new ByteArrayOutputStream(8192)) {
            IOUtils.copy(data, out);

            buf = ByteBuffer.allocateDirect(out.size());
            buf.put(out.toByteArray());
            buf.flip();
        } finally {
            data.close();
        }

        NbtInByteBuffer in = new NbtInByteBuffer(buf);

        return NbtFastIo.read(in);
    }


    /**
     * @author JellySquid
     */
    @Overwrite
    public void setTagAt(ChunkPos pos, CompoundTag tag) throws IOException {
        RegionFile file = this.getRegionFile(pos);

        NbtOut out = new NbtOutByteBuffer(8192);
        NbtFastIo.write(tag, out);

        ((RegionFileNbtBuf) file).write(pos, out);
    }
}
