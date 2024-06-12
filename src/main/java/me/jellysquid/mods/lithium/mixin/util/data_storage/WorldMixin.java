package me.jellysquid.mods.lithium.mixin.util.data_storage;

import me.jellysquid.mods.lithium.common.world.LithiumData;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(World.class)
public class WorldMixin implements LithiumData {

    @Unique
    private final LithiumData.Data storage = new LithiumData.Data();
    @Override
    public LithiumData.Data lithium$getData() {
        return this.storage;
    }
}
