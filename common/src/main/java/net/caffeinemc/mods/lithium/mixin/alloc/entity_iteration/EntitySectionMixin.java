package net.caffeinemc.mods.lithium.mixin.alloc.entity_iteration;

import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.world.level.entity.EntitySection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Iterator;

@Mixin(EntitySection.class)
public class EntitySectionMixin {

    @Redirect(
            method = "getEntities(Lnet/minecraft/world/phys/AABB;Lnet/minecraft/util/AbortableIterationConsumer;)Lnet/minecraft/util/Continuation;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ClassInstanceMultiMap;iterator()Ljava/util/Iterator;")
    )
    private Iterator<?> directIterator(ClassInstanceMultiMap<?> instance) {
        return ((ClassInstanceMultiMapAccessor<?>) instance).getAllInstances().iterator();
    }
}
