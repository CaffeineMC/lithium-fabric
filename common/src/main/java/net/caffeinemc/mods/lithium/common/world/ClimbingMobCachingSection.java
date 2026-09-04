package net.caffeinemc.mods.lithium.common.world;

import net.caffeinemc.mods.lithium.common.entity.pushable.EntityPushablePredicate;
import net.caffeinemc.mods.lithium.common.entity.pushable.FeetBlockCachingEntity;
import net.minecraft.util.Continuation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;

public interface ClimbingMobCachingSection {

    Continuation lithium$collectPushableEntities(Level world, Entity except, AABB box, EntityPushablePredicate<? super Entity> entityPushablePredicate, ArrayList<Entity> entities);

    void lithium$onEntityModifiedCachedBlock(FeetBlockCachingEntity entity, BlockState newBlockState);
}
