package me.jellysquid.mods.lithium.mixin.util.item_type_tracking;

import me.jellysquid.mods.lithium.common.util.change_tracking.ChangePublisher;
import me.jellysquid.mods.lithium.common.util.change_tracking.ChangeSubscriber;
import net.minecraft.component.ComponentMapImpl;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ChangePublisher<ItemStack>, ChangeSubscriber<ComponentMapImpl> {
    @Shadow
    @Nullable
    public abstract Entity getHolder();

    @Shadow
    @Final
    ComponentMapImpl components;

    @Override
    public boolean lithium$subscribe(ChangeSubscriber<ItemStack> subscriber) {
        //noinspection ObjectEquality,EqualsBetweenInconvertibleTypes
        if (subscriber == this.getHolder()) {
            this.trackChanges();
            return true;
        } else {
            return false;
        }
    }

    @Unique
    private void trackChanges() {
        //Safe because ComponentMapImplMixin
        //noinspection unchecked
        boolean b = ((ChangePublisher<ComponentMapImpl>) (Object) this.components).lithium$subscribe(this);
        if (!b) {
            throw new IllegalStateException("Failed to subscribe to component map!");
        }
    }

    @Override
    public void lithium$unsubscribe(ChangeSubscriber<ItemStack> subscriber) {
        //noinspection ObjectEquality,EqualsBetweenInconvertibleTypes
        if (subscriber == this.getHolder()) {
            //Safe because ComponentMapImplMixin
            //noinspection unchecked
            ((ChangePublisher<ComponentMapImpl>) (Object) this.components).lithium$unsubscribe(this);
        }
    }

    @Inject(method = "setHolder(Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"))
    private void unsubscribePrevious(Entity holder, CallbackInfo ci) {
        if (this.getHolder() instanceof ChangeSubscriber<?> subscriber && this.getHolder() != holder) {
            subscriber.lithium$forceUnsubscribe(null);
        }
    }

    @Inject(method = "setCount(I)V", at = @At("HEAD"))
    private void unsubscribeOnEmpty(int count, CallbackInfo ci) {
        //Safe because ComponentMapImplMixin
        //noinspection unchecked
        ((ChangePublisher<ComponentMapImpl>) (Object) this.components).lithium$unsubscribe(this);

        if (this.getHolder() instanceof ChangeSubscriber<?> subscriber) {
            subscriber.lithium$forceUnsubscribe(null);
        }
    }

    @Override
    public void lithium$notify(ComponentMapImpl publisher) {
        if (this.getHolder() instanceof ChangeSubscriber<?> subscriber) {
            subscriber.lithium$notify(null);
        }
    }
}
