package me.jellysquid.mods.lithium.mixin.region.mmap_files;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.RegionFile;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

@Mixin(RegionFile.class)
public abstract class MixinRegionFile {
    @Shadow
    @Final
    private RandomAccessFile file;

    private FileChannel fc;

    private MappedByteBuffer mmapOffsets, mmapTimestamps;

    @Shadow
    @Final
    private List<Boolean> sectorFree;

    @Shadow
    protected abstract int getPackedRegionRelativePosition(ChunkPos pos);

    /**
     * Prevents any reading from taking place, short-circuiting the two loops which populate the offset and timestamp
     * tables.
     */
    @ModifyConstant(method = "<init>",
            constant = @Constant(intValue = 1024),
            slice = @Slice(from = @At(value = "INVOKE", target = "Ljava/io/RandomAccessFile;seek(J)V")))
    private static int modifyInitField(int unused) {
        return 0;
    }

    /**
     * Sets up the memory-mapped regions.
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void reinit(File file_1, CallbackInfo ci) throws IOException {
        this.fc = this.file.getChannel();

        this.mmapOffsets = this.fc.map(FileChannel.MapMode.READ_WRITE, 0, 1024 * 4);
        this.mmapTimestamps = this.fc.map(FileChannel.MapMode.READ_WRITE, 4096, 1024 * 4);

        for (int i = 0; i < 1024; ++i) {
            int offset = this.mmapOffsets.getInt(i * 4);

            if (offset != 0 && (offset >> 8) + (offset & 255) <= this.sectorFree.size()) {
                for (int j = 0; j < (offset & 255); ++j) {
                    this.sectorFree.set((offset >> 8) + j, false);
                }
            }
        }
    }

    /**
     * @reason Use memory-mapped files to read the offsets.
     * @author JellySquid
     */
    @Overwrite
    private int getOffset(ChunkPos pos) {
        return this.mmapOffsets.getInt(this.getPackedRegionRelativePosition(pos) * 4);
    }

    /**
     * @reason Use memory-mapped files to modify the offsets.
     * @author JellySquid
     */
    @Overwrite
    private void setOffset(ChunkPos pos, int offset) {
        this.mmapOffsets.putInt(this.getPackedRegionRelativePosition(pos) * 4, offset);
    }

    /**
     * @reason Use memory-mapped files to modify the timestamps.
     * @author JellySquid
     */
    @Overwrite
    private void setTimestamp(ChunkPos pos, int stamp) {
        this.mmapTimestamps.putInt(this.getPackedRegionRelativePosition(pos) * 4, stamp);
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void onClose(CallbackInfo ci) throws IOException {
        this.mmapOffsets.force();
        this.mmapTimestamps.force();

        this.fc.force(false);
        this.fc.close();
    }

}
