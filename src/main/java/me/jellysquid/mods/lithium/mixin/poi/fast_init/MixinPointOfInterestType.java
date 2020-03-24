package me.jellysquid.mods.lithium.mixin.poi.fast_init;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import me.jellysquid.mods.lithium.common.world.interests.PointOfInterestTypeHelper;
import net.minecraft.block.BlockState;
import net.minecraft.world.poi.PointOfInterestType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(PointOfInterestType.class)
public class MixinPointOfInterestType {
    @Mutable
    @Shadow
    @Final
    private static Map<BlockState, PointOfInterestType> BLOCK_STATE_TO_POINT_OF_INTEREST_TYPE;

    static {
        BLOCK_STATE_TO_POINT_OF_INTEREST_TYPE = new Reference2ReferenceOpenHashMap<>(BLOCK_STATE_TO_POINT_OF_INTEREST_TYPE);

        PointOfInterestTypeHelper.init(new ObjectArraySet<>(BLOCK_STATE_TO_POINT_OF_INTEREST_TYPE.keySet()));
    }
}
