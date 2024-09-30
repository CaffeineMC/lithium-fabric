package me.jellysquid.mods.lithium.mixin.util.entity_collection_replacement;

import me.jellysquid.mods.lithium.common.entity.TypeFilterableListInternalAccess;
import net.minecraft.util.ClassInstanceMultiMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.*;
import java.util.function.Function;

@Mixin(ClassInstanceMultiMap.class)
public abstract class TypeFilterableListMixin<T> extends AbstractCollection<T> implements TypeFilterableListInternalAccess<T> {

    @Shadow
    @Final
    private Map<Class<?>, List<T>> byClass;

    @Shadow
    public abstract <S> Collection<S> find(Class<S> type);

    @Override
    public <S extends T> List<S> lithium$getOrCreateAllOfTypeRaw(Class<S> type) {
        //noinspection unchecked
        List<S> s = (List<S>) this.byClass.get(type);
        if (s == null) {
            this.find(type);
            //noinspection unchecked
            s = (List<S>) this.byClass.get(type);
        }
        return s;
    }

    @Override
    public <S extends T> List<S> lithium$replaceCollectionAndGet(Class<S> type, Function<ArrayList<S>, List<S>> listCtor) {
        List<T> oldList = this.byClass.get(type);
        //noinspection unchecked
        List<S> newList = listCtor.apply((ArrayList<S>) oldList);
        //noinspection unchecked
        this.byClass.put(type, (List<T>) newList);
        return newList;
    }

    @Override
    public <S extends T> List<S> lithium$replaceCollectionAndGet(Class<S> type, ArrayList<S> list) {
        //noinspection unchecked
        this.byClass.put(type, (List<T>) list);
        return list;
    }
}
