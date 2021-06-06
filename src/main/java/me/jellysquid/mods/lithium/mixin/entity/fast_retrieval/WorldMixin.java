package me.jellysquid.mods.lithium.mixin.entity.fast_retrieval;

import me.jellysquid.mods.lithium.common.util.collections.IteratorLazyList;
import me.jellysquid.mods.lithium.common.world.WorldHelper;
import net.minecraft.entity.Entity;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.entity.EntityLookup;
import net.minecraft.world.entity.SectionedEntityCache;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

@Mixin(World.class)
public abstract class WorldMixin {
    @Shadow
    public abstract Profiler getProfiler();

    @Shadow
    protected abstract EntityLookup<Entity> getEntityLookup();

    /**
     * @author 2No2Name
     * @reason use lazy evaluation and reduce lambdas
     */
    @Overwrite
    public List<Entity> getOtherEntities(@Nullable Entity except, Box box, Predicate<? super Entity> predicate) {
        this.getProfiler().visit("getEntities");
        //noinspection unchecked
        SectionedEntityCache<Entity> cache = ((SimpleEntityLookupAccessor<Entity>) this.getEntityLookup()).getCache();
        Iterator<Entity> entityIterator = WorldHelper.iterateEntitiesOfTypeFilter(cache, WorldHelper.UNFILTERED, box, predicate, except);
        return new IteratorLazyList<>(entityIterator);
    }

    /**
     * @author 2No2Name
     * @reason use lazy evaluation and reduce lambdas
     */
    @Overwrite
    public <T extends Entity> List<T> getEntitiesByType(TypeFilter<Entity, T> filter, Box box, Predicate<? super T> predicate) {
        this.getProfiler().visit("getEntities");
        //noinspection unchecked
        SectionedEntityCache<Entity> cache = ((SimpleEntityLookupAccessor<Entity>) this.getEntityLookup()).getCache();
        Iterator<T> entityIterator = WorldHelper.iterateEntitiesOfTypeFilter(cache, filter, box, predicate, null);
        return new IteratorLazyList<>(entityIterator);
    }
}
