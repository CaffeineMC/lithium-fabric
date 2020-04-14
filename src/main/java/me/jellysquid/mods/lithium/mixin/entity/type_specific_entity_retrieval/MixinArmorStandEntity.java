package me.jellysquid.mods.lithium.mixin.entity.type_specific_entity_retrieval;

import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.function.Predicate;

@Mixin(ArmorStandEntity.class)
public class MixinArmorStandEntity {
    @Shadow
    @Final
    private static Predicate<Entity> RIDEABLE_MINECART_PREDICATE;

    @Redirect(method = "tickCramming", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getEntities(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;)Ljava/util/List;"))
    private List<Entity> getMinecartsDirectly(World world, Entity entity_1, Box box_1, Predicate<? super Entity> predicate_1){
        if (predicate_1 == RIDEABLE_MINECART_PREDICATE) {
            //not using MinecartEntity.class and no predicate, because mods may add another minecart that is type ridable without being Minecartentity
            return world.getEntities(AbstractMinecartEntity.class, box_1, (Entity e) -> e != entity_1 && ((AbstractMinecartEntity)e).getMinecartType() == AbstractMinecartEntity.Type.RIDEABLE);
        }
        return world.getEntities(entity_1, box_1, predicate_1);
    }
}
