package me.jellysquid.mods.lithium.mixin.entity.type_specific_entity_retrieval;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.FormCaravanGoal;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.function.Predicate;

@Mixin(FormCaravanGoal.class)
public class MixinFormCaravanGoal {
    @Redirect(method = "canStart", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getEntities(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;)Ljava/util/List;"))
    private List<Entity> getLlamasForCaravan(World world, Entity entity_1, Box box_1, Predicate<? super Entity> predicate_1) {
        return world.getEntities(LlamaEntity.class, box_1, (Entity e) -> e != entity_1);
    }
}
