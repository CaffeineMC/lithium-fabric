package me.jellysquid.mods.lithium.mixin.ai.fast_brain;

import me.jellysquid.mods.lithium.common.util.IIterableWeightedList;
import net.minecraft.util.collection.WeightedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.List;

@Mixin(WeightedList.class)
public class MixinWeightedList<U> implements IIterableWeightedList<U> {
    @Shadow
    @Final
    protected List<WeightedList<U>.Entry<? extends U>> entries;

    @Override
    public Iterator<U> iterator() {
        return new IIterableWeightedList.ListIterator<>(this.entries.iterator());
    }
}
