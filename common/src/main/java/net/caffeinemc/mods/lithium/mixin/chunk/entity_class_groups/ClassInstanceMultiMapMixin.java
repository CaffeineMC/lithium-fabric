package net.caffeinemc.mods.lithium.mixin.chunk.entity_class_groups;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceArrayMap;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import net.caffeinemc.mods.lithium.common.entity.EntityClassGroup;
import net.caffeinemc.mods.lithium.common.world.chunk.ClassGroupFilterableList;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Patches {@link ClassInstanceMultiMap} to allow grouping entities by arbitrary groups of classes instead of one class only.
 */
@Mixin(ClassInstanceMultiMap.class)
public abstract class ClassInstanceMultiMapMixin<T> implements ClassGroupFilterableList<T> {

    @Shadow
    @Final
    private List<T> allInstances;

    private final Reference2ReferenceArrayMap<EntityClassGroup, ReferenceLinkedOpenHashSet<T>> entitiesByGroup =
            new Reference2ReferenceArrayMap<>();

    /**
     * Update our collections
     */
    @ModifyVariable(method = "add(Ljava/lang/Object;)Z", at = @At("HEAD"), argsOnly = true)
    public T add(T entity) {
        for (Map.Entry<EntityClassGroup, ReferenceLinkedOpenHashSet<T>> entityGroupAndSet : this.entitiesByGroup.entrySet()) {
            EntityClassGroup entityGroup = entityGroupAndSet.getKey();
            if (entityGroup.contains(((Entity) entity).getClass())) {
                entityGroupAndSet.getValue().add((entity));
            }
        }
        return entity;
    }

    /**
     * Update our collections
     */
    @ModifyVariable(method = "remove(Ljava/lang/Object;)Z", at = @At("HEAD"), argsOnly = true)
    public Object remove(Object o) {
        for (ReferenceLinkedOpenHashSet<T> entitySet : this.entitiesByGroup.values()) {
            //noinspection SuspiciousMethodCalls
            entitySet.remove(o);
        }
        return o;
    }

    /**
     * Get entities of a class group
     */
    public Collection<T> lithium$getAllOfGroupType(EntityClassGroup type) {
        Collection<T> collection = this.entitiesByGroup.get(type);

        if (collection == null) {
            collection = this.createAllOfGroupType(type);
        }

        return collection;
    }

    /**
     * Start grouping by a new class group
     */
    private Collection<T> createAllOfGroupType(EntityClassGroup type) {
        ReferenceLinkedOpenHashSet<T> allOfType = new ReferenceLinkedOpenHashSet<>();

        for (T entity : this.allInstances) {
            if (type.contains(entity.getClass())) {
                allOfType.add(entity);
            }
        }
        this.entitiesByGroup.put(type, allOfType);

        return allOfType;
    }
}