package me.jellysquid.mods.lithium.mixin.cached_hashcode;

import net.minecraft.server.world.ChunkTicket;
import net.minecraft.server.world.ChunkTicketType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkTicket.class)
public class MixinChunkTicket<T> {
    @Shadow
    @Final
    private ChunkTicketType<T> type;

    @Shadow
    @Final
    private int level;

    @Shadow
    @Final
    private T argument;

    private int hashCode;

    /**
     * @reason Initialize the object's hashcode and cache it
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstructed(ChunkTicketType<T> type, int level, T argument, CallbackInfo ci) {
        int hash = 1;
        hash = 31 * hash + this.type.hashCode();
        hash = 31 * hash + this.level;
        hash = 31 * hash + this.argument.hashCode();

        this.hashCode = hash;
    }

    /**
     * @reason Uses the cached hashcode
     * @author JellySquid
     */
    @Overwrite
    public int hashCode() {
        return this.hashCode;
    }
}
