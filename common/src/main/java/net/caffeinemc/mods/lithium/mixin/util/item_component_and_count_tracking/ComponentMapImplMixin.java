package net.caffeinemc.mods.lithium.mixin.util.item_component_and_count_tracking;

import net.caffeinemc.mods.lithium.common.util.change_tracking.ChangePublisher;
import net.caffeinemc.mods.lithium.common.util.change_tracking.ChangeSubscriber;
import net.minecraft.core.component.PatchedDataComponentMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PatchedDataComponentMap.class)
public class ComponentMapImplMixin implements ChangePublisher<PatchedDataComponentMap> {

    @Unique
    private ChangeSubscriber<PatchedDataComponentMap> subscriber;

    @Override
    public void lithium$subscribe(ChangeSubscriber<PatchedDataComponentMap> subscriber, int subscriberData) {
        if (subscriberData != 0) {
            throw new UnsupportedOperationException("ComponentMapImpl does not support subscriber data");
        }
        this.subscriber = ChangeSubscriber.combine(this.subscriber, 0, subscriber, 0);
    }

    @Override
    public int lithium$unsubscribe(ChangeSubscriber<PatchedDataComponentMap> subscriber) {
        this.subscriber = ChangeSubscriber.without(this.subscriber, subscriber);
        return 0;
    }

    @Inject(
            method = "ensureMapOwnership()V", at = @At("HEAD")
    )
    private void trackBeforeChange(CallbackInfo ci) {
        if (this.subscriber != null) {
            this.subscriber.lithium$notify((PatchedDataComponentMap) (Object) this, 0);
        }
    }
}
