package me.jellysquid.mods.lithium.mixin.entity.simple_entity_block_collisions;

import me.jellysquid.mods.lithium.common.shapes.LithiumEntityCollisions;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.ViewableWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.stream.Stream;

@Mixin(ViewableWorld.class)
public interface MixinViewableWorld {
    /**
     * @reason Use a faster implementation
     * @author JellySquid
     */
    @Overwrite
    default Stream<VoxelShape> method_20812(final Entity entity, Box box) {
        return LithiumEntityCollisions.method_20812((ViewableWorld) this, entity, box);
    }
}