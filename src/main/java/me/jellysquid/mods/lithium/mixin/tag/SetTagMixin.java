package me.jellysquid.mods.lithium.mixin.tag;

import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.tag.SetTag;
import net.minecraft.tag.Tag;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(SetTag.class)
public abstract class SetTagMixin<T> implements Tag<T> {
    @Shadow @Final @Mutable
    private Set<T> valueSet;

    /**
     * If the number of elements in a tag is very small (<=3), it can be significantly faster to use simple linear scanning
     * across an array to check if an element is contained by the tag. We mix into the implementation type for Tag
     * and try replacing the type of set after the constructor has ran. If the set is too large, we still replace it
     * with a faster set type which has reference equality semantics.
     *
     * @reason Use specialized implementations
     * @author JellySquid
     */
    // Plugin has trouble seeing this, but it exists
    @Inject(method = "<init>(Ljava/util/Set;Ljava/lang/Class;)V", at = @At("RETURN"))
    private void init(Set<T> set, Class<?> var2, CallbackInfo ci) {
        // Reference equality is safe for tag values
        // Use linear-scanning when the number of items in the tag is small
        if (this.valueSet.size() <= 3) {
            this.valueSet = new ReferenceArraySet<>(this.valueSet);
        } else {
            this.valueSet = new ReferenceOpenHashSet<>(this.valueSet);
        }
    }
}
