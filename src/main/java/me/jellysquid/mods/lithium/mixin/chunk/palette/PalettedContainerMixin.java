package me.jellysquid.mods.lithium.mixin.chunk.palette;

import me.jellysquid.mods.lithium.common.world.chunk.LithiumHashPalette;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.*;

@Mixin(PalettedContainer.PaletteProvider.class)
public abstract class PalettedContainerMixin {
    @Mutable
    @Shadow
    @Final
    public static PalettedContainer.PaletteProvider BLOCK_STATE;

    @Unique
    private static final Palette.Factory HASH = LithiumHashPalette::create;

    @Shadow
    @Final
    static Palette.Factory ID_LIST;

    /**
     * @reason Replace the hash palette from vanilla with our own and change the threshold for usage to only 3 bits,
     * as our implementation performs better at smaller key ranges.
     * @author JellySquid
     */

    static {
        BLOCK_STATE = new PalettedContainer.PaletteProvider(4) {
            public <A> PalettedContainer.DataProvider<A> createDataProvider(IndexedIterable<A> idList, int bits) {
                return switch (bits) {
                    case 0 -> new PalettedContainer.DataProvider<>(SINGULAR, bits);

                    // Bits 1-4 must all pass 4 as parameter, otherwise chunk sections will corrupt.
                    case 1, 2 -> new PalettedContainer.DataProvider<>(ARRAY, 4);

                    case 3, 4 -> new PalettedContainer.DataProvider<>(HASH, 4);

                    case 5, 6, 7, 8 -> new PalettedContainer.DataProvider<>(HASH, bits);

                    default -> new PalettedContainer.DataProvider<>(ID_LIST, MathHelper.ceilLog2(idList.size()));
                };
            }
        };
    }
}
