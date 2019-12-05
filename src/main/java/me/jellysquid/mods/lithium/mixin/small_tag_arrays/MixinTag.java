package me.jellysquid.mods.lithium.mixin.small_tag_arrays;

import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.Set;

@Mixin(Tag.class)
public class MixinTag<T> {
    @Shadow
    @Final
    private Set<T> values;

    private T[] valuesSmallArray;

    @Inject(method = "<init>(Lnet/minecraft/util/Identifier;Ljava/util/Collection;Z)V", at = @At("RETURN"))
    private void postConstructed(Identifier id, Collection<Tag.Entry<T>> collection, boolean ordered, CallbackInfo ci) {
        if (this.values.size() < 6) {
            //noinspection unchecked
            this.valuesSmallArray = (T[]) this.values.toArray();
        }
    }

    /**
     * Makes use of the small values array for quicker indexing if the number of elements is small. This can improve
     * tag matching performance significantly for tags with only one or two objects.
     *
     * @reason Use array scanning when the number of elements is small
     * @author JellySquid
     */
    @Overwrite
    public boolean contains(T obj) {
        if (this.valuesSmallArray != null) {
            for (T other : this.valuesSmallArray) {
                if (other == obj) {
                    return true;
                }
            }

            return false;
        } else {
            return this.values.contains(obj);
        }
    }

}
