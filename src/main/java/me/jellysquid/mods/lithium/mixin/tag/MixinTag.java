package me.jellysquid.mods.lithium.mixin.tag;

import me.jellysquid.mods.lithium.common.util.tag.ArrayTag;
import me.jellysquid.mods.lithium.common.util.tag.HashTag;
import net.minecraft.tag.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Set;

@Mixin(Tag.class)
public interface MixinTag {
    /**
     * If the number of elements in a tag is very small, it can be significantly faster to use simple linear scanning
     * across an array to check if an element is contained by the tag.
     *
     * Currently, Mixins can only be used to overwrite a static method in an interface. It would be preferable to
     * inject at HEAD and return a specialized implementation there rather than overwriting the method.
     *
     * @reason Use specialized implementations
     * @author JellySquid
     */
    @Overwrite
    static <T> Tag<T> of(Set<T> set) {
        if (set.size() <= 4) {
            return new ArrayTag<>(set);
        } else {
            return new HashTag<>(set);
        }
    }
}
