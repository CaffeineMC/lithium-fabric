package me.jellysquid.mods.lithium.mixin.ai.nearby_entity_tracking;

import net.minecraft.entity.Entity;
import net.minecraft.world.entity.EntityChangeListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor
    EntityChangeListener getEntityChangeListener();
}
