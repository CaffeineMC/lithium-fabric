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
    //check if the mixin of typefilterablelist is loaded, otherwise don't use it
    public interface MixinLoadTest {}
    public static final boolean CUSTOM_TYPE_FILTERABLE_LIST_DISABLED = !MixinLoadTest.class.isAssignableFrom(TypeFilterableList.class);

    
    /**
     * Partial [VanillaCopy] Classes overriding Entity.getHardCollisionBox(Entity other) or Entity.getCollisionBox()
     * The returned entity list is only used to call getCollisionBox and getHardCollisionBox. As most entities return null
     * for both of these methods, getting those is not necessary. This is why we only get entities when they overwrite
     * getCollisionBox
     * @param entityView the world
     * @param selection the box the entities have to collide with
     * @param entity the entity that is searching for the colliding entities
     * @return list of entities with collision boxes
     */
    public static List<Entity> getEntitiesWithCollisionBoxForEntity(EntityView entityView, Box selection, Entity entity) {
        if (CUSTOM_TYPE_FILTERABLE_LIST_DISABLED || entity != null && EntityClassGroup.HARD_COLLISION_BOX_OVERRIDE.contains(entity.getClass()) || !(entityView instanceof World)) {
            //use vanilla code when getHardCollisionBox(Entity other) is overwritten, as every entity could be relevant as argument of getHardCollisionBox
            return entityView.getEntities(entity, selection);
        } else {
            //only get entities that overwrite getCollisionBox
            return WorldHelper.getEntitiesOfClassGroup((World)entityView, entity, EntityClassGroup.COLLISION_BOX_OVERRIDE, selection, EXCEPT_SPECTATOR);
        }
    }

    /**
     * Interface to group entity types that don't always return null on getCollisionBox.
     */
    public interface CollisionBoxOverridingEntity {}
}
