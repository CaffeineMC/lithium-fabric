package me.jellysquid.mods.lithium.mixin.collections.nbt;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.HashMap;
import java.util.Map;

@Mixin(NbtCompound.class)
public class NbtCompoundMixin {

    @Shadow
    @Final
    private Map<String, NbtElement> entries;

    @ModifyArg(
            method = "<init>()V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtCompound;<init>(Ljava/util/Map;)V")
    )
    private static Map<String, NbtElement> useFasterCollection(Map<String, NbtElement> oldMap) {
        return new Object2ObjectOpenHashMap<>();
    }

    @Redirect(
            method = "<init>()V",
            at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;")
    )
    private static <K, V> HashMap<K, V> removeOldMapAlloc() {
        return null;
    }

    /**
     * @reason Use faster collection
     * @author Maity
     */
    @Overwrite
    public NbtCompound copy() {
        // [VanillaCopy] HashMap is replaced with Object2ObjectOpenHashMap
        var map = new Object2ObjectOpenHashMap<>(Maps.transformValues(this.entries, NbtElement::copy));
        return new NbtCompound(map);
    }
}
