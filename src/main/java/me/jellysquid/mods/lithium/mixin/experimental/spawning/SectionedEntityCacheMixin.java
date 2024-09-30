package me.jellysquid.mods.lithium.mixin.experimental.spawning;

import com.google.common.collect.AbstractIterator;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import me.jellysquid.mods.lithium.common.world.ChunkAwareEntityIterable;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntitySectionStorage;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;

@Mixin(EntitySectionStorage.class)
public abstract class SectionedEntityCacheMixin<T extends EntityAccess> implements ChunkAwareEntityIterable<T> {

    @Shadow
    @Final
    private Long2ObjectMap<EntitySection<T>> sections;

    @Override
    public Iterable<T> lithium$IterateEntitiesInTrackedSections() {
        ObjectCollection<EntitySection<T>> sections = this.sections.values();
        return () -> {
            ObjectIterator<EntitySection<T>> sectionsIterator = sections.iterator();
            return new AbstractIterator<T>() {
                Iterator<T> entityIterator;
                @Nullable
                @Override
                protected T computeNext() {
                    if (this.entityIterator != null && this.entityIterator.hasNext()) {
                        return this.entityIterator.next();
                    }
                    while (sectionsIterator.hasNext()) {
                        EntitySection<T> section = sectionsIterator.next();
                        if (section.getStatus().isAccessible() && !section.isEmpty()) {
                            //noinspection unchecked
                            this.entityIterator = ((EntityTrackingSectionAccessor<T>) section).getCollection().iterator();
                            if (this.entityIterator.hasNext()) {
                                return this.entityIterator.next();
                            }
                        }
                    }
                    return this.endOfData();
                }
            };
        };
    }

}
