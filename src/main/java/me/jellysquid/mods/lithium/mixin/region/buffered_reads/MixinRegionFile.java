package me.jellysquid.mods.lithium.mixin.region.buffered_reads;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.RegionFile;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

@Mixin(RegionFile.class)
public abstract class MixinRegionFile {
    @Shadow
    @Final
    private int[] offsets;

    @Shadow
    @Final
    private RandomAccessFile file;

    @Shadow
    @Final
    private int[] chunkTimestamps;

    @Shadow
    @Final
    private List<Boolean> sectorFree;

    @Shadow
    protected abstract int getOffset(ChunkPos pos);

    /**
     * Prevents any reading from taking place, short-circuiting two loops.
     */
    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 1024), slice = @Slice(
            from = @At(value = "INVOKE", target = "Ljava/io/RandomAccessFile;seek(J)V")
    ))
    private static int modifyInitField(int unused) {
        return 0;
    }

    /**
     * Perform the read initialization we canceled just prior, but this time with one large buffered read. This makes it
     * so we only have to perform one I/O call versus two thousand of them.
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void reinit(File file_1, CallbackInfo ci) throws IOException {
        byte[] bufBytes = new byte[(1024 * 2) * 4];

        this.file.readFully(bufBytes);

        ByteBuffer buf = ByteBuffer.wrap(bufBytes);

        for (int i = 0; i < 1024; ++i) {
            int offset = buf.getInt();

            this.offsets[i] = offset;

            if (offset != 0 && (offset >> 8) + (offset & 255) <= this.sectorFree.size()) {
                for (int j = 0; j < (offset & 255); ++j) {
                    this.sectorFree.set((offset >> 8) + j, false);
                }
            }
        }

        for (int i = 0; i < 1024; ++i) {
            this.chunkTimestamps[i] = buf.getInt();
        }
    }

    /**
     * We can't get around overwriting this method as it is too complex. This re-implements the function with a single
     * large read.
     * <p>
     * Note: You might notice that there are a lot of checks here which will simply return null if they fail. This mimics
     * the behavior of vanilla exactly... we've not changed anything. It also means that in case anything goes wrong,
     * the game will simply generate a new chunk, likely overwriting the unreadable one. Perhaps someone should make
     * these checks fail with an exception...
     *
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

}
