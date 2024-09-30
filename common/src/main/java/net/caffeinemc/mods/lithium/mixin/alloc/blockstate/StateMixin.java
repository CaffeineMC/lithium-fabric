package net.caffeinemc.mods.lithium.mixin.alloc.blockstate;

import com.google.common.collect.Table;
import net.caffeinemc.mods.lithium.common.state.FastImmutableTable;
import net.caffeinemc.mods.lithium.common.state.StatePropertyTableCache;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(StateHolder.class)
public class StateMixin<O, S> {
    @Shadow
    private Table<Property<?>, Comparable<?>, S> neighbours;

    @Shadow
    @Final
    protected O owner;

    @Inject(method = "populateNeighbours(Ljava/util/Map;)V", at = @At("RETURN"))
    private void postCreateWithTable(Map<Map<Property<?>, Comparable<?>>, S> states, CallbackInfo ci) {
        if (this.owner instanceof Block || this.owner instanceof Fluid) {
            this.neighbours = new FastImmutableTable<>(this.neighbours, StatePropertyTableCache.getTableCache(this.owner));
        }
    }

}
