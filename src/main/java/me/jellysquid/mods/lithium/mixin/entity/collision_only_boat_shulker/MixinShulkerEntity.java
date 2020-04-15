package me.jellysquid.mods.lithium.mixin.entity.collision_only_boat_shulker;

import me.jellysquid.mods.lithium.common.entity.LithiumEntityCollisions;
import net.minecraft.entity.mob.ShulkerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ShulkerEntity.class)
public class MixinShulkerEntity implements LithiumEntityCollisions.CollisionBoxOverridingEntity { }
