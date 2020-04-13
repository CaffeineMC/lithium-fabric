package me.jellysquid.mods.lithium.mixin.entity.use_classed_collision_box_retrieval;

import me.jellysquid.mods.lithium.common.entity.LithiumEntityCollisions;
import net.minecraft.entity.vehicle.BoatEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BoatEntity.class)
public class MixinBoatEntity implements LithiumEntityCollisions.CollisionBoxOverridingEntity { }
