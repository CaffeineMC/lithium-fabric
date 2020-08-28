package me.jellysquid.mods.lithium.common.entity.movement;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.CollisionView;

public interface BlockCollisionPredicate {
    BlockCollisionPredicate ANY = (world, pos, state) -> true;
    BlockCollisionPredicate SUFFOCATES = (world, pos, state) -> state.shouldSuffocate(world, pos);

    /**
     * @param world The world of which collision tests are being performed in
     * @param pos The position of the block in the world
     * @param state The block state that is being collided with
     * @return True if the block can be collided with, otherwise false
     */
    boolean test(CollisionView world, BlockPos pos, BlockState state);
}
