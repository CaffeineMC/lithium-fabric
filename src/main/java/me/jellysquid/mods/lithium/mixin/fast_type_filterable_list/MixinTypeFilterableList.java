package me.jellysquid.mods.lithium.mixin.fast_type_filterable_list;

import net.minecraft.util.TypeFilterableList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Patches {@link TypeFilterableList} to improve performance when entities are being queried in the world.
 */
@Mixin(TypeFilterableList.class)
public class MixinTypeFilterableList<T>  {
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
     * we can assume it's already valid.
     * @author JellySquid
     */
    @SuppressWarnings("unchecked")
    @Overwrite
    public <S> Collection<S> getAllOfType(Class<S> type) {
        List<T> list = this.elementsByType.computeIfAbsent(type, (t) -> {
            if (!this.elementType.isAssignableFrom(t)) {
                throw new IllegalArgumentException("Don't know how to search for " + t);
            }

            return this.allElements.stream().filter(t::isInstance).collect(Collectors.toList());
        });

        return (Collection<S>) Collections.unmodifiableCollection(list);
    }
}
