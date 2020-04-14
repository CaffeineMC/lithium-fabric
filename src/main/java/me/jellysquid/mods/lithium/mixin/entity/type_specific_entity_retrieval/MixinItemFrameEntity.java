package me.jellysquid.mods.lithium.mixin.entity.type_specific_entity_retrieval;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.function.Predicate;

@Mixin(ItemFrameEntity.class)
public abstract class MixinItemFrameEntity extends AbstractDecorationEntity {
    protected MixinItemFrameEntity(EntityType<? extends AbstractDecorationEntity> entityType_1, World world_1) {
        super(entityType_1, world_1);
    }

    @Redirect(method = "canStayAttached", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getEntities(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;)Ljava/util/List;"))
    private List<Entity> getAbstractDecorationEntities(World world, Entity entity_1, Box box_1, Predicate<? super Entity> predicate_1) {
        if (predicate_1 == PREDICATE) {
            return world.getEntities(AbstractDecorationEntity.class, box_1, (Entity e) -> e != entity_1);
        }
        return world.getEntities(entity_1, box_1, predicate_1);
    }
}
