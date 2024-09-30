package me.jellysquid.mods.lithium.mixin.collections.entity_ticking;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityTickList;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityTickList.class)
public class EntityListMixin {
    @Shadow
    private @Nullable Int2ObjectMap<Entity> iterated;

    @Shadow
    private Int2ObjectMap<Entity> active;

    @Shadow
    private Int2ObjectMap<Entity> passive;

    /**
     * @author 2No2Name
     * @reason avoid slow iteration, allocate instead
     */
    @Overwrite
    private void ensureActiveIsNotIterated() {
        if (this.iterated == this.active) {
            this.passive = this.active;
            this.active = ((Int2ObjectLinkedOpenHashMap<Entity>) this.active).clone();
        }
    }
}
