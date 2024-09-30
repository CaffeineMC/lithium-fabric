package me.jellysquid.mods.lithium.mixin.shapes.blockstate_cache;

import me.jellysquid.mods.lithium.common.util.collections.Object2BooleanCacheTable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Block.class)
public class BlockMixin {
    private static final Object2BooleanCacheTable<VoxelShape> FULL_CUBE_CACHE = new Object2BooleanCacheTable<>(
            512,
            shape -> !Shapes.joinIsNotEmpty(Shapes.block(), shape, BooleanOp.NOT_SAME)
    );

    /**
     * @reason Use a faster cache implementation
     * @author gegy1000
     */
    @Overwrite
    public static boolean isShapeFullBlock(VoxelShape shape) {
        return FULL_CUBE_CACHE.get(shape);
    }
}
