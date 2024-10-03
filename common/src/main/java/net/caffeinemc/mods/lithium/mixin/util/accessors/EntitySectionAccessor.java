package net.caffeinemc.mods.lithium.mixin.util.accessors;

import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.world.level.entity.EntitySection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntitySection.class)
public interface EntitySectionAccessor<T> {
    @Accessor("storage")
    ClassInstanceMultiMap<T> getCollection();
}
