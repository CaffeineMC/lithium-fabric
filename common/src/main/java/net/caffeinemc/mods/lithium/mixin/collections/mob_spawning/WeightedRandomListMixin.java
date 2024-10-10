package net.caffeinemc.mods.lithium.mixin.collections.mob_spawning;

import com.google.common.collect.ImmutableList;
import net.caffeinemc.mods.lithium.common.util.collections.HashedReferenceList;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;

@Mixin(WeightedRandomList.class)
public class WeightedRandomListMixin<E extends WeightedEntry> {

    @Mutable
    @Shadow
    @Final
    private ImmutableList<E> items;
    //Need a separate variable due to entries being type ImmutableList
    @Unique
    private List<E> entryHashList;

    @Inject(method = "<init>(Ljava/util/List;)V", at = @At("RETURN"))
    private void init(List<? extends E> entries, CallbackInfo ci) {
        //We are using reference equality here, because all vanilla implementations of Weighted use reference equality
        this.entryHashList = this.items.size() > 4 ? Collections.unmodifiableList(new HashedReferenceList<>(this.items)) : this.items;
    }

    /**
     * @author 2No2Name
     * @reason return a collection with a faster contains() call
     */
    @Overwrite
    public List<E> unwrap() {
        return this.entryHashList;
    }
}
