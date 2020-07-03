package me.jellysquid.mods.lithium.mixin.tag;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.tag.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(targets = "net/minecraft/tag/Tag$1")
public abstract class TagImplMixin<T> implements Tag<T> {
    // Synthetic field -- plugin cannot see it
    @SuppressWarnings("ShadowTarget")
    @Shadow(remap = false)
    private Set<T> field_23686;

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
    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(method = "<init>(Ljava/util/Set;Lcom/google/common/collect/ImmutableList;)V", at = @At("RETURN"))
    private void init(Set<T> set, ImmutableList<T> list, CallbackInfo ci) {
        // Reference equality is safe for tag values
        // Use linear-scanning when the number of items in the tag is small
        if (this.field_23686.size() <= 3) {
            this.field_23686 = new ReferenceArraySet<>(this.field_23686);
        } else {
            this.field_23686 = new ReferenceOpenHashSet<>(this.field_23686);
        }
    }
}
