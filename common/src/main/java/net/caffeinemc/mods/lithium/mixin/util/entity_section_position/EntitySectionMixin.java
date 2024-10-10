package net.caffeinemc.mods.lithium.mixin.util.entity_section_position;

import net.caffeinemc.mods.lithium.common.entity.PositionedEntityTrackingSection;
import net.minecraft.world.level.entity.EntitySection;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntitySection.class)
public class EntitySectionMixin implements PositionedEntityTrackingSection {
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
