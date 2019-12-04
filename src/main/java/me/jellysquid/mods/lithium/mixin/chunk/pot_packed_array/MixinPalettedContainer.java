package me.jellysquid.mods.lithium.mixin.chunk.pot_packed_array;

import me.jellysquid.mods.lithium.common.util.LithiumMath;
import me.jellysquid.mods.lithium.common.util.palette.POTPackedIntegerArray;
import net.minecraft.util.PackedIntegerArray;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PalettedContainer.class)
public abstract class MixinPalettedContainer<T> {
    @Shadow
    public abstract void lock();

    @Shadow
    private int paletteSize;

    @Shadow
    private Palette<T> palette;

    @Shadow
    protected PackedIntegerArray data;

    @Shadow
    public abstract void unlock();

    @Redirect(method = "setPaletteSize", at = @At(value = "NEW", target = "net/minecraft/util/PackedIntegerArray"))
    private PackedIntegerArray replaceIntegerArray(int bits, int size) {
        return new POTPackedIntegerArray(bits, size);
    }

    @ModifyVariable(method = "setPaletteSize", at = @At("HEAD"), index = 1, name = "int_1")
    private int afterPaletteSizeChanged(int old) {
        return LithiumMath.nextPowerOfTwo(old);
    }


    /**
     * We need to serialize the data in the vanilla format, so we use an intermediary array.
     *
     * @author JellySquid
     */
    @Overwrite
    public void toPacket(PacketByteBuf buf) {
        this.lock();

        buf.writeByte(this.paletteSize);

        this.palette.toPacket(buf);

        final PackedIntegerArray array = new PackedIntegerArray(this.paletteSize, 4096);

        for (int i = 0; i < this.data.getSize(); i++) {
            array.set(i, this.data.get(i));
        }

        buf.writeLongArray(array.getStorage());

        this.unlock();
    }

}
