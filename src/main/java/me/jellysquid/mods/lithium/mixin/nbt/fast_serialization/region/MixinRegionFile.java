package me.jellysquid.mods.lithium.mixin.nbt.fast_serialization.region;

import me.jellysquid.mods.lithium.common.nbt.region.RegionFileDirectWritable;
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
import java.util.zip.Deflater;

@Mixin(RegionFile.class)
public abstract class MixinRegionFile implements RegionFileDirectWritable {
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
    public void write(ChunkPos pos, byte[] bytes) throws IOException {
        this.deflater.reset();
        this.deflater.setInput(bytes);
        this.deflater.finish();

        byte[] compressed;

        try (ByteArrayOutputStream bout = new ByteArrayOutputStream(bytes.length)) {
            while (!this.deflater.finished()) {
                int count = this.deflater.deflate(this.tmp);

                bout.write(this.tmp, 0, count);
            }

            compressed = bout.toByteArray();
        }

        this.write(pos, compressed, compressed.length);
    }

}
