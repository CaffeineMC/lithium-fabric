package me.jellysquid.mods.lithium.mixin.ai.nearby_entity_tracking;

import me.jellysquid.mods.lithium.common.entity.tracker.nearby.EntityWithNearbyListener;
import me.jellysquid.mods.lithium.common.entity.tracker.nearby.NearbyEntityListenerMulti;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Extends the base living entity class to provide a {@link NearbyEntityListenerMulti} which will handle the
 * child {@link EntityWithNearbyListener}s of AI tasks attached to this entity.
 */
@Mixin(LivingEntity.class)
public class MixinLivingEntity implements EntityWithNearbyListener {
    private NearbyEntityListenerMulti tracker;

    /**
     * Initialize the entity listener.
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(EntityType<? extends LivingEntity> type, World world, CallbackInfo ci) {
        this.tracker = new NearbyEntityListenerMulti();
    }

    @Override
    public NearbyEntityListenerMulti getListener() {
        return this.tracker;
    }
}
