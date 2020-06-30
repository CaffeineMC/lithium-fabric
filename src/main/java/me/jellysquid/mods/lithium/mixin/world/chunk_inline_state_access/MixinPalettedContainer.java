package me.jellysquid.mods.lithium.mixin.world.chunk_inline_state_access;

import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PalettedContainer.class)
public class MixinPalettedContainer<T> {
    @Shadow
    @Final
    private T field_12935;
    @Shadow
    private PackedIntegerArray data;
    @Shadow
    private Palette<T> palette;

    /**
     * @reason Help JVM to optimize code by reducing instructions
     * @author Maity
     */
    @Overwrite
    public T get(int x, int y, int z) {
        // this.get(toIndex(x, y, z))
        T o = this.palette.getByIndex(this.data.get(y << 8 | z << 4 | x));
        return o == null ? this.field_12935 : o;
    }
}
