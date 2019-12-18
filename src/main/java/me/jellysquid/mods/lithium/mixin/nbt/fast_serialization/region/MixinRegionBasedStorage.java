package me.jellysquid.mods.lithium.mixin.nbt.fast_serialization.region;

import me.jellysquid.mods.lithium.common.nbt.io.NbtFastIo;
import me.jellysquid.mods.lithium.common.nbt.io.bytes.NbtByteArrayReader;
import me.jellysquid.mods.lithium.common.nbt.io.unsafe.NbtUnsafeWriter;
import me.jellysquid.mods.lithium.common.nbt.region.RegionFileDirectWritable;
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

@SuppressWarnings("OverwriteModifiers")
@Mixin(RegionBasedStorage.class)
public abstract class MixinRegionBasedStorage {
    private static final int INITIAL_SIZE = 1024 * 32;

    @Shadow
    protected abstract RegionFile getRegionFile(ChunkPos pos) throws IOException;

    /**
     * @reason Use optimized NBT serialization
     * @author JellySquid
     */
    @Overwrite
    public CompoundTag getTagAt(ChunkPos pos) throws IOException {
        RegionFile file = this.getRegionFile(pos);

        InputStream data = file.getChunkDataInputStream(pos);

        if (data == null) {
            return null;
        }

        byte[] buf;

        try (ByteArrayOutputStream out = new ByteArrayOutputStream(INITIAL_SIZE)) {
            IOUtils.copy(data, out);

            buf = out.toByteArray();
        } finally {
            data.close();
        }

        NbtByteArrayReader in = new NbtByteArrayReader(buf);

        return NbtFastIo.read(in);
    }


    /**
     * @reason Use optimized NBT serialization
     * @author JellySquid
     */
    @Overwrite
    public void setTagAt(ChunkPos pos, CompoundTag tag) throws IOException {
        RegionFile file = this.getRegionFile(pos);

        byte[] bytes;

        try (NbtUnsafeWriter writer = new NbtUnsafeWriter(INITIAL_SIZE)) {
            NbtFastIo.write(tag, writer);
            bytes = writer.finish();
        }

        ((RegionFileDirectWritable) file).write(pos, bytes);
    }
}
