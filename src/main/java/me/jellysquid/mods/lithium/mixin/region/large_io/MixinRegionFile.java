package me.jellysquid.mods.lithium.mixin.region.large_io;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.RegionFile;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

@Mixin(RegionFile.class)
public abstract class MixinRegionFile {
    @Shadow
    @Final
    private RandomAccessFile file;

    @Shadow
    @Final
    private List<Boolean> sectorFree;

    @Shadow
    protected abstract int getOffset(ChunkPos pos);

    /**
     * We can't get around overwriting this method as it is too complex. This re-implements the function with a single
     * large read.
     * <p>
     * Note: You might notice that there are a lot of checks here which will simply return null if they fail. This mimics
     * the behavior of vanilla exactly... we've not changed anything. It also means that in case anything goes wrong,
     * the game will simply generate a new chunk, likely overwriting the unreadable one. Perhaps someone should make
     * these checks fail with an exception...
     *
     * @reason Replace smaller reads with one large read
     * @author JellySquid
     */
    @SuppressWarnings("OverwriteModifiers")
    @Overwrite
    public synchronized DataInputStream getChunkDataInputStream(ChunkPos pos) throws IOException {
        int offset = this.getOffset(pos);

        if (offset == 0) {
            return null;
        }

        int sector = offset >> 8;
        int len = offset & 255;

        if (sector + len > this.sectorFree.size()) {
            return null;
        }

        byte[] bytes = new byte[4096 * len];

        this.file.seek(sector * 4096);
        this.file.readFully(bytes);

        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        int reportedLen = buffer.getInt();

        if (reportedLen > bytes.length) {
            return null;
        } else if (reportedLen <= 0) {
            return null;
        }

        byte compressionType = buffer.get();

        if (compressionType == 1) {
            return new DataInputStream(new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(bytes, buffer.position(), bytes.length))));
        } else if (compressionType == 2) {
            return new DataInputStream(new BufferedInputStream(new InflaterInputStream(new ByteArrayInputStream(bytes, buffer.position(), bytes.length))));
        }

        return null;
    }

    /**
     * @reason Write data in one operation
     * @author JellySquid
     */
    @Overwrite
    private void write(int index, byte[] bytes, int len) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(len + 5);
        buf.putInt(len + 1); // Header: Size
        buf.put((byte) 2); // Header: Version
        buf.put(bytes, 0, len); // Data

        this.file.seek(index * 4096);
        this.file.write(buf.array());
    }
}
