package me.jellysquid.mods.lithium.mixin.util.item_type_tracking;

import me.jellysquid.mods.lithium.common.util.change_tracking.ChangePublisher;
import me.jellysquid.mods.lithium.common.util.change_tracking.ChangeSubscriber;
import net.minecraft.component.ComponentMapImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ComponentMapImpl.class)
public class ComponentMapImplMixin implements ChangePublisher<ComponentMapImpl> {

    @Shadow
    private boolean copyOnWrite;
    @Unique
    private ChangeSubscriber<ComponentMapImpl> subscriber;

    @Override
    public boolean lithium$subscribe(ChangeSubscriber<ComponentMapImpl> subscriber) {
        this.startTrackingChanges();
        this.subscriber = ChangeSubscriber.add(this.subscriber, subscriber);
        return true;
    }

    @Override
    public void lithium$unsubscribe(ChangeSubscriber<ComponentMapImpl> subscriber) {
        this.subscriber = ChangeSubscriber.remove(this.subscriber, subscriber);
    }

    @Unique
    private void startTrackingChanges() {
        this.copyOnWrite = true; // Not necessary, but we are careful in case other mods are otherwise skipping calling onWrite()
    }

    @Inject(
            method = "onWrite()V", at = @At("HEAD")
    )
    private void trackBeforeChange(CallbackInfo ci) {
        if (this.subscriber != null) {
            this.subscriber.lithium$notify((ComponentMapImpl) (Object) this);
        }
    }
}
