package me.jellysquid.mods.lithium.mixin.chunk.section_update_tracking;

import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import net.minecraft.server.world.ChunkHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChunkHolder.class)
public class ChunkHolderMixin {

    @Shadow
    @Final
    private ShortSet[] blockUpdatesBySection;

    @Shadow
    private boolean pendingBlockUpdates;

    /**
     * Using Hashsets instead of ArraySets for better worst-case performance
     * The default case of just a few items may be very slightly slower
     */
    @ModifyVariable(
            method = "markForBlockUpdate",
            at = @At(
                    ordinal = 0,
                    value = "FIELD",
                    target = "Lnet/minecraft/server/world/ChunkHolder;blockUpdatesBySection:[Lit/unimi/dsi/fastutil/shorts/ShortSet;",
                    shift = At.Shift.BEFORE
            )
    )
    private int createShortHashSet(int b) {
        if (blockUpdatesBySection[b] == null) {
            this.pendingBlockUpdates = true;
            this.blockUpdatesBySection[b] = new ShortOpenHashSet();
        }
        return b;
    }
}
