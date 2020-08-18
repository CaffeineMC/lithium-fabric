package me.jellysquid.mods.lithium.mixin.chunk.collidableEntityList;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import me.jellysquid.mods.lithium.common.entity.EntityClassGroup;
import me.jellysquid.mods.lithium.common.entity.EntityClassGroupHelper;
import me.jellysquid.mods.lithium.common.world.chunk.ClassGroupFilterableList;
import net.minecraft.entity.Entity;
import net.minecraft.util.collection.TypeFilterableList;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

/**
 * Patches {@link TypeFilterableList} to improve performance when entities are being queried in the world.
 * Patches it also to allow grouping entity types.
 */
@Mixin(TypeFilterableList.class)
public abstract class TypeFilterableListMixin<T> implements ClassGroupFilterableList<T>, EntityClassGroupHelper.MixinLoadTest {

    @Shadow
    @Final
    private List<T> allElements;


    private Reference2ReferenceOpenHashMap<EntityClassGroup, ReferenceLinkedOpenHashSet<T>> entitiesByGroup;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(Class<T> elementType, CallbackInfo ci) {
        this.entitiesByGroup = new Reference2ReferenceOpenHashMap<>();
    }

    /**
     * Update our collections
     */
    @ModifyVariable(method = "add", at = @At("HEAD"), argsOnly = true)
    public T add(T entity) {
        for (Map.Entry<EntityClassGroup, ReferenceLinkedOpenHashSet<T>> entityGroupAndSet : this.entitiesByGroup.entrySet()) {
            EntityClassGroup entityGroup = entityGroupAndSet.getKey();
            if (entityGroup.contains(((Entity)entity).getClass())) {
                entityGroupAndSet.getValue().add((entity));
            }
        }
        return entity;
    }

    /**
     * Update our collections
     */
    @ModifyVariable(method = "remove", at = @At("HEAD"), argsOnly = true)
    public Object remove(Object o) {
        for (Map.Entry<EntityClassGroup, ReferenceLinkedOpenHashSet<T>> entityGroupAndSet : this.entitiesByGroup.entrySet()) {
            entityGroupAndSet.getValue().remove(o);
        }

        return o;
    }

    /**
     * Get entities of the given class group
     */
    public Collection<T> getAllOfGroupType(EntityClassGroup type) {
        Collection<T> collection = this.entitiesByGroup.get(type);

        if (collection == null) {
            collection =  this.createAllOfGroupType(type);
        }

        return Collections.unmodifiableCollection(collection);
    }

    /**
     * Start grouping by a new class group
     */
    private <S> Collection<T> createAllOfGroupType(EntityClassGroup type) {
        ReferenceLinkedOpenHashSet<T> allOfType = new ReferenceLinkedOpenHashSet<>();

        for (T entity : this.allElements) {
            if (type.contains(entity.getClass())) {
                allOfType.add(entity);
            }
        }
        this.entitiesByGroup.put(type, allOfType);
        return allOfType;
    }
}
