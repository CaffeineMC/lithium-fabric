package me.jellysquid.mods.lithium.mixin.chunk.pot_packed_array.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.PackedIntegerArray;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PalettedContainer.class)
public abstract class MixinPalettedContainer<T> {
    @Shadow
    public abstract void lock();

    @Shadow
    private int paletteSize;

    @Shadow
    protected abstract void setPaletteSize(int size);

    @Shadow
    private Palette<T> palette;

    @Shadow
    protected PackedIntegerArray data;

    @Shadow
    public abstract void unlock();

    /**
     * @reason Decode the chunk data in the vanilla format with an intermediate array
     * @author JellySquid
     */
    @Overwrite
    @Environment(EnvType.CLIENT)
    public void fromPacket(PacketByteBuf buf) {
        this.lock();

        int size = buf.readByte();

        if (this.paletteSize != size) {
            this.setPaletteSize(size);
        }

        this.palette.fromPacket(buf);

        final PackedIntegerArray array = new PackedIntegerArray(size, 4096, buf.readLongArray(null));

        for (int i = 0; i < array.getSize(); i++) {
            this.data.set(i, array.get(i));
        }

        this.unlock();
    }

}
