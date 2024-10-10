package net.caffeinemc.mods.lithium.mixin.alloc.nbt;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.HashMap;
import java.util.Map;

/**
 * Use {@link Object2ObjectOpenHashMap} instead of {@link HashMap} to reduce NBT memory consumption and improve
 * iteration speed.
 *
 * @author Maity
 */
@Mixin(CompoundTag.class)
public class CompoundTagMixin {

    @Shadow
    @Final
    private Map<String, Tag> tags;

    @ModifyArg(
            method = "<init>()V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;<init>(Ljava/util/Map;)V")
    )
    private static Map<String, Tag> useFasterCollection(Map<String, Tag> oldMap) {
        return new Object2ObjectOpenHashMap<>();
    }

    @Redirect(
            method = "<init>()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;",
                    remap = false
            )
    )
    private static HashMap<?, ?> removeOldMapAlloc() {
        return null;
    }

    /**
     * @reason Use faster collection
     * @author Maity
     */
    @Overwrite
    public CompoundTag copy() {
        // [VanillaCopy] HashMap is replaced with Object2ObjectOpenHashMap
        var map = new Object2ObjectOpenHashMap<>(Maps.transformValues(this.tags, Tag::copy));
        return new CompoundTag(map);
    }

    @Mixin(targets = "net/minecraft/nbt/CompoundTag$1")
    static class Type {

        @ModifyVariable(
                method = "loadCompound",
                at = @At(
                        value = "INVOKE_ASSIGN",
                        target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;",
                        remap = false
                )
        )
        private static Map<String, Tag> useFasterCollection(Map<String, Tag> map) {
            return new Object2ObjectOpenHashMap<>();
        }

        @Redirect(
                method = "loadCompound",
                at = @At(
                        value = "INVOKE",
                        target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;",
                        remap = false
                )
        )
        private static HashMap<?, ?> removeOldMapAlloc() {
            return null;
        }
    }
}
