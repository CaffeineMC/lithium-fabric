package me.jellysquid.mods.lithium.mixin.chunk.palette;

import me.jellysquid.mods.lithium.common.world.chunk.LithiumHashPalette;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.IdList;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PaletteResizeListener;
import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Function;

/**
 * Patches {@link PalettedContainer} to make use of {@link LithiumHashPalette}.
 */
@Mixin(value = PalettedContainer.class, priority = 999)
public abstract class PalettedContainerMixin<T> {
    @Shadow
    private Palette<T> palette;

    @Shadow
    protected PackedIntegerArray data;

    @Shadow
    protected abstract void set(int int_1, T object_1);

    @Shadow
    private int paletteSize;

    @Shadow
    @Final
    private Function<NbtCompound, T> elementDeserializer;

    @Shadow
    @Final
    private Function<T, NbtCompound> elementSerializer;

    @Shadow
    @Final
    private IdList<T> idList;

    @Shadow
    @Final
    private Palette<T> fallbackPalette;

    @Shadow
    protected abstract T get(int int_1);

    /**
     * @reason Replace the hash palette from vanilla with our own and change the threshold for usage to only 3 bits,
     * as our implementation performs better at smaller key ranges.
     * @author JellySquid
     *
     * Overwrite replaced with 3 mixins by 2No2Name
     */

    @ModifyConstant(
            method = "setPaletteSize(I)V",
            constant = @Constant(intValue = 9)
    )
    private int skipBiMapPalette(int size) {
        return -1;
    }
    @Redirect(
            method = "setPaletteSize(I)V",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/chunk/PalettedContainer;fallbackPalette:Lnet/minecraft/world/chunk/Palette;"
            )
    )
    private Palette<T> hashOrFallbackPalette(PalettedContainer<T> palettedContainer) {
        if (this.paletteSize < 9) {
            //noinspection unchecked
            return new LithiumHashPalette<>(this.idList, this.paletteSize, (PaletteResizeListener<T>) this, this.elementDeserializer, this.elementSerializer);
        }
        return this.fallbackPalette;
    }
    @Redirect(
            method = "setPaletteSize(I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/math/MathHelper;log2DeBruijn(I)I"
            )
    )
    private int hashOrFallbackPaletteSize(int value) {
        if (this.palette != this.fallbackPalette) {
            return this.paletteSize;
        }
        return MathHelper.log2DeBruijn(value);
    }
}
