package me.jellysquid.mods.lithium.common.entity;

import me.jellysquid.mods.lithium.common.world.WorldHelper;
import net.minecraft.entity.Entity;
import net.minecraft.util.collection.TypeFilterableList;
import net.minecraft.util.math.Box;
import net.minecraft.world.EntityView;
import net.minecraft.world.World;

import java.util.List;

import static net.minecraft.predicate.entity.EntityPredicates.EXCEPT_SPECTATOR;

public class EntityClassGroupHelper {
    //check if the EntityClassGroup Mixin of TypeFilterableList is loaded, otherwise use the vanilla implementation
    public interface MixinLoadTest {}
    public static final boolean CUSTOM_TYPE_FILTERABLE_LIST_DISABLED = !MixinLoadTest.class.isAssignableFrom(TypeFilterableList.class);

    
    /**
     * Partial [VanillaCopy] Classes overriding Entity.getHardCollisionBox(Entity other) or Entity.getCollisionBox()
     * The returned entity list is only used to call getCollisionBox and getHardCollisionBox. As most entities return null
     * for both of these methods, getting those is not necessary. This is why we only get entities when they overwrite
     * getCollisionBox
     * @param entityView the world
     * @param box the box the entities have to collide with
     * @param collidingEntity the entity that is searching for the colliding entities
     * @return list of entities with collision boxes
     */
    public static List<Entity> getEntitiesWithCollisionBoxForEntity(EntityView entityView, Box box, Entity collidingEntity) {
        if (CUSTOM_TYPE_FILTERABLE_LIST_DISABLED || collidingEntity != null && EntityClassGroup.HARD_COLLISION_BOX_OVERRIDE.contains(collidingEntity.getClass()) || !(entityView instanceof World)) {
            //use vanilla code when method_30949 (previously getHardCollisionBox(Entity other)) is overwritten, as every entity could be relevant as argument of getHardCollisionBox
            return entityView.getOtherEntities(collidingEntity, box);
        } else {
            //only get entities that overwrite method_30948 (previously getCollisionBox)
            return WorldHelper.getEntitiesOfClassGroup((World)entityView, collidingEntity, EntityClassGroup.COLLISION_BOX_OVERRIDE, box, EXCEPT_SPECTATOR);
        }
    }
}
