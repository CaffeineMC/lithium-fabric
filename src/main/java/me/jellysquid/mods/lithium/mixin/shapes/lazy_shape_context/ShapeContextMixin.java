package me.jellysquid.mods.lithium.mixin.shapes.lazy_shape_context;

import me.jellysquid.mods.lithium.common.block.LithiumEntityShapeContext;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ShapeContext.class)
public interface ShapeContextMixin {
    /**
     * @author 2No2Name
     * @reason be faster
     */
    @Overwrite
    static ShapeContext of(Entity entity) {
        return new LithiumEntityShapeContext(entity);
    }
}
