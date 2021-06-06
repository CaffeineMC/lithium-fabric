package me.jellysquid.mods.lithium.mixin.entity.fast_retrieval;

import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.entity.SectionedEntityCache;
import net.minecraft.world.entity.SimpleEntityLookup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SimpleEntityLookup.class)
public interface SimpleEntityLookupAccessor<T extends EntityLike> {

    @Accessor("cache")
    SectionedEntityCache<T> getCache();
}
