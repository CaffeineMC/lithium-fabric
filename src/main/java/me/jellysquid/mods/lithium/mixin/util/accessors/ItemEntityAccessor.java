package me.jellysquid.mods.lithium.mixin.util.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;
import net.minecraft.world.entity.item.ItemEntity;

@Mixin(ItemEntity.class)
public interface ItemEntityAccessor {
    @Accessor("target")
    UUID lithium$getOwner();
}
