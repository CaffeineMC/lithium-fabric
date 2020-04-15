package me.jellysquid.mods.lithium.mixin.entity.replace_entitytype_predicates;

import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.function.Predicate;

@Mixin(AbstractDecorationEntity.class)
public class MixinAbstractDecorationEntity {
    @Shadow
    @Final
    protected static Predicate<Entity> PREDICATE;

    @Redirect(method = "canStayAttached", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getEntities(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;)Ljava/util/List;"))
    private List<Entity> getAbstractDecorationEntities(World world, Entity entity_1, Box box_1, Predicate<? super Entity> predicate_1) {
        if (predicate_1 == PREDICATE) {
            return world.getEntities(AbstractDecorationEntity.class, box_1, (Entity e) -> e != entity_1);
        }
        return world.getEntities(entity_1, box_1, predicate_1);
    }
}
