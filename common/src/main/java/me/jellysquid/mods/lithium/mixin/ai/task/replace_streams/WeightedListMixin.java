package me.jellysquid.mods.lithium.mixin.ai.task.replace_streams;

import me.jellysquid.mods.lithium.common.ai.WeightedListIterable;
import net.minecraft.world.entity.ai.behavior.ShufflingList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.List;

@Mixin(ShufflingList.class)
public class WeightedListMixin<U> implements WeightedListIterable<U> {
    @Shadow
    @Final
    protected List<ShufflingList.WeightedEntry<? extends U>> entries;

    @Override
    public Iterator<U> iterator() {
        return new WeightedListIterable.ListIterator<>(this.entries.iterator());
    }
}
