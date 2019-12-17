package me.jellysquid.mods.lithium.mixin.nbt.fast_serialization.region;

import me.jellysquid.mods.lithium.common.nbt.io.NbtOut;
import me.jellysquid.mods.lithium.common.nbt.region.RegionFileNbtBuf;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.RegionFile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.Deflater;

@Mixin(RegionFile.class)
public abstract class MixinRegionFile implements RegionFileNbtBuf {
    private Deflater deflater;
    private byte[] tmp;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(File file, CallbackInfo ci) {
        this.deflater = new Deflater();
        this.deflater.setLevel(Deflater.BEST_SPEED);

        this.tmp = new byte[8192];
    }

    @Shadow
    protected abstract void write(ChunkPos chunkPos, byte[] bs, int i) throws IOException;

    @Override
    public void write(ChunkPos pos, NbtOut out) throws IOException {
        ByteBuffer buf = out.finish();

        this.deflater.reset();
        this.deflater.setInput(buf);
        this.deflater.finish();

        byte[] compressed;

        try (ByteArrayOutputStream bout = new ByteArrayOutputStream(buf.limit())) {
            while (!this.deflater.finished()) {
                int count = deflater.deflate(this.tmp);

                bout.write(this.tmp, 0, count);
            }

            compressed = bout.toByteArray();
        }

        this.write(pos, compressed, compressed.length);
    }
}
