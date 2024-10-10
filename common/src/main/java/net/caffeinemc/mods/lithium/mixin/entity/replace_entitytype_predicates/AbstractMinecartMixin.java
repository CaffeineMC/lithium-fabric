package net.caffeinemc.mods.lithium.mixin.entity.replace_entitytype_predicates;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(AbstractMinecart.class)
public class AbstractMinecartMixin {

    @Redirect(
            method = "tick()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"
            )
    )
    private List<AbstractMinecart> getOtherAbstractMinecarts(Level world, Entity except, AABB box) {
        return world.getEntitiesOfClass(AbstractMinecart.class, box, entity -> entity != except);
    }
}
