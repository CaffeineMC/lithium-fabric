package me.jellysquid.mods.lithium.mixin.world.fast_type_filterable_list;

import net.minecraft.util.collection.TypeFilterableList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.*;

/**
 * Patches {@link TypeFilterableList} to improve performance when entities are being queried in the world.
 */
@Mixin(TypeFilterableList.class)
public class MixinTypeFilterableList<T> {
    @Shadow
    @Final
    private Class<T> elementType;

    @Shadow
    @Final
    private Map<Class<?>, List<T>> elementsByType;

    @Shadow
    @Final
    private List<T> allElements;

    /**
     * @reason Only perform the slow Class#isAssignableFrom(Class) if a list doesn't exist for the type, otherwise
     * we can assume it's already valid. The slow-path code is moved to a separate method to help the JVM inline this.
     * @author JellySquid
     */
    @SuppressWarnings("unchecked")
    @Overwrite
    public <S> Collection<S> getAllOfType(Class<S> type) {
        Collection<T> collection = this.elementsByType.get(type);

        if (collection == null) {
            collection = this.createAllOfType(type);
        }

        return (Collection<S>) Collections.unmodifiableCollection(collection);
    }

    private <S> Collection<T> createAllOfType(Class<S> type) {
        if (!this.elementType.isAssignableFrom(type)) {
            throw new IllegalArgumentException("Don't know how to search for " + type);
        }

        List<T> list = new ArrayList<>();

        for (T allElement : this.allElements) {
            if (type.isInstance(allElement)) {
                list.add(allElement);
            }
        }

        this.elementsByType.put(type, list);

        return list;
    }
}
