package net.caffeinemc.mods.lithium.mixin.ai.poi;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.caffeinemc.mods.lithium.common.world.interests.types.PointOfInterestTypeHelper;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

/**
 * Replaces the backing map type with a faster collection type which uses reference equality.
 */
@Mixin(PoiTypes.class)
public class PointOfInterestTypesMixin {
    @Mutable
    @Shadow
    @Final
    private static Map<BlockState, PoiType> TYPE_BY_STATE;

    static {
        TYPE_BY_STATE = new Reference2ReferenceOpenHashMap<>(TYPE_BY_STATE);

        PointOfInterestTypeHelper.init(TYPE_BY_STATE.keySet());
    }
}
