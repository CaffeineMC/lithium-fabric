package me.jellysquid.mods.lithium.mixin.util.entity_section_position;

import me.jellysquid.mods.lithium.common.entity.PositionedEntityTrackingSection;
import net.minecraft.world.entity.EntityTrackingSection;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityTrackingSection.class)
public class EntityTrackingSectionMixin implements PositionedEntityTrackingSection {
    private long pos;

    @Override
    public void lithium$setPos(long chunkSectionPos) {
        this.pos = chunkSectionPos;
    }

    @Override
    public long lithium$getPos() {
        return this.pos;
    }
}
