package me.jellysquid.mods.lithium.mixin.collections.entity_filtering;

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
import org.spongepowered.asm.mixin.injection.Redirect;
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
    private Class<T> elementType;
    @Shadow
    @Final @Mutable
    private List<T> allElements;
    @Shadow
    @Final @Mutable
    private Map<Class<?>, List<T>> elementsByType;

    //replace the ArrayList, has slow remove
    private Reference2ReferenceOpenHashMap<Object, ReferenceLinkedOpenHashSet<T>> entitiesByGroup;
    //cached unmodifiables of the above entityByGroup sets
    private Map<Object, Collection<T>> entitiesByGroupUnmodifiables;
    private ReferenceLinkedOpenHashSet<T> allEntities;



    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(Class<T> elementType, CallbackInfo ci) {
        this.entitiesByGroup = new Reference2ReferenceOpenHashMap<>();
        this.entitiesByGroupUnmodifiables = new Reference2ReferenceOpenHashMap<>();
        this.allEntities = new ReferenceLinkedOpenHashSet<>();
        this.entitiesByGroup.put(this.elementType, this.allEntities);
        this.entitiesByGroupUnmodifiables.put(this.elementType, Collections.unmodifiableCollection(this.allEntities));

        this.allElements = null;
        this.elementsByType = null;
    }

    /**
     * @reason use our collections, no simple redirect to our iterator, as we have no lists
     * @author 2No2Name
     */
    @Overwrite
    public boolean add(T entity) {
        boolean bl = false;
        for (Map.Entry<Object, ReferenceLinkedOpenHashSet<T>> entityGroupAndMap : this.entitiesByGroup.entrySet()) {
            Object entityGroup = entityGroupAndMap.getKey();
            if (entityGroup instanceof Class) {
                if (((Class)entityGroup).isInstance(entity)) {
                    entityGroupAndMap.getValue().add(entity);
                    bl = true;
                }
            } else {
                if (((EntityClassGroup)entityGroup).contains(((Entity)entity).getClass())) {
                    entityGroupAndMap.getValue().add((entity));
                    bl = true;
                }
            }
        }
        return bl;
    }

    /**
     * @reason use our collections, no simple redirect to our iterator, as we have no lists
     * @author 2No2Name
     */
    @Overwrite
    public boolean remove(Object o) {
        boolean bl = false;

        for (Map.Entry<Object, ReferenceLinkedOpenHashSet<T>> objectReferenceLinkedOpenHashSetEntry : this.entitiesByGroup.entrySet()) {
            bl |= objectReferenceLinkedOpenHashSetEntry.getValue().remove((T) o);
        }

        return bl;
    }


    /**
     * @reason Only perform the slow Class#isAssignableFrom(Class) if a list doesn't exist for the type, otherwise
     * we can assume it's already valid. The slow-path code is moved to a separate method to help the JVM inline this.
     * @author JellySquid
     */
    @SuppressWarnings("unchecked")
    @Overwrite
    public <S> Collection<S> getAllOfType(Class<S> type) {
        Collection<T> collection = this.entitiesByGroupUnmodifiables.get(type);

        if (collection == null) {
            collection =  this.createAllOfType(type);
        }

        return (Collection<S>) collection;
    }

    public Collection<T> getAllOfGroupType(EntityClassGroup type) {
        Collection<T> collection = this.entitiesByGroupUnmodifiables.get(type);

        if (collection == null) {
            collection =  this.createAllOfGroupType(type);
        }

        return collection;
    }

    @Redirect(method = "iterator", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z"))
    private boolean isEmpty(List list) {
        return this.allEntities.isEmpty();
    }
    @Redirect(method = "iterator", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"))
    private Iterator<T> iterator(List list) {
        return this.allEntities.iterator();
    }
    @Redirect(method = "size", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"))
    private int size(List list) {
        return this.allEntities.size();
    }

    private <S> Collection<T> createAllOfType(Class<S> type) {
        if (!this.elementType.isAssignableFrom(type)) {
            throw new IllegalArgumentException("Don't know how to search for " + type);
        }

        ReferenceLinkedOpenHashSet<T> allOfType = new ReferenceLinkedOpenHashSet<>();
        Collection<T> allOfTypeUnmodifiable = Collections.unmodifiableCollection(allOfType);

        for (T entity : this.allEntities) {
            if (type.isInstance(entity)) {
                allOfType.add(entity);
            }
        }

        this.entitiesByGroup.put(type, allOfType);
        this.entitiesByGroupUnmodifiables.put(type, allOfTypeUnmodifiable);


        return allOfTypeUnmodifiable;
    }

    private <S> Collection<T> createAllOfGroupType(EntityClassGroup type) {
        ReferenceLinkedOpenHashSet<T> allOfType = new ReferenceLinkedOpenHashSet<>();
        Collection<T> allOfTypeUnmodifiable = Collections.unmodifiableCollection(allOfType);

        for (T entity : this.allEntities) {
            if (type.contains(entity.getClass())) {
                allOfType.add(entity);
            }
        }

        this.entitiesByGroup.put(type, allOfType);
        this.entitiesByGroupUnmodifiables.put(type, allOfTypeUnmodifiable);

        return allOfTypeUnmodifiable;
    }

    /**
     * @author JellySquid
     * @reason Do not copy the list every call to provide immutability, instead wrap with an unmodifiable type
     */
    @Overwrite
    public List<T> method_29903() {
        return Collections.unmodifiableList(this.allElements);
    }
}
