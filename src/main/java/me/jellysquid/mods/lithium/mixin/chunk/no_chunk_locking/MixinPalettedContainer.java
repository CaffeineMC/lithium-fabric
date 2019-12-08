package me.jellysquid.mods.lithium.mixin.chunk.no_chunk_locking;

import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(PalettedContainer.class)
public class MixinPalettedContainer {
    /**
     * @reason Do not check the container's lock
     * @author JellySquid
     */
    @Overwrite
    public void lock() {

    }

    /**
     * @reason Do not check the container's lock
     * @author JellySquid
     */
    @Overwrite
    public void unlock() {

    }
}
