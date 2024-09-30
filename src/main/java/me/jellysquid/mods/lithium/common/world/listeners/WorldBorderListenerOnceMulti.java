package me.jellysquid.mods.lithium.common.world.listeners;

import java.util.WeakHashMap;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;

public class WorldBorderListenerOnceMulti implements BorderChangeListener {

    private final WeakHashMap<WorldBorderListenerOnce, Object> delegate;

    public WorldBorderListenerOnceMulti() {
        this.delegate = new WeakHashMap<>();
    }

    public void add(WorldBorderListenerOnce listener) {
        this.delegate.put(listener, null);
    }

    public void onAreaReplaced(WorldBorder border) {
        for (WorldBorderListenerOnce listener : this.delegate.keySet()) {
            listener.onAreaReplaced(border);
        }
        this.delegate.clear();
    }

    @Override
    public void onBorderSizeSet(WorldBorder border, double size) {
        for (WorldBorderListenerOnce listener : this.delegate.keySet()) {
            listener.onBorderSizeSet(border, size);
        }
        this.delegate.clear();
    }

    @Override
    public void onBorderSizeLerping(WorldBorder border, double fromSize, double toSize, long time) {
        for (WorldBorderListenerOnce listener : this.delegate.keySet()) {
            listener.onBorderSizeLerping(border, fromSize, toSize, time);
        }
        this.delegate.clear();
    }

    @Override
    public void onBorderCenterSet(WorldBorder border, double centerX, double centerZ) {
        for (WorldBorderListenerOnce listener : this.delegate.keySet()) {
            listener.onBorderCenterSet(border, centerX, centerZ);
        }
        this.delegate.clear();
    }

    @Override
    public void onBorderSetWarningTime(WorldBorder border, int warningTime) {
        for (WorldBorderListenerOnce listener : this.delegate.keySet()) {
            listener.onBorderSetWarningTime(border, warningTime);
        }
        this.delegate.clear();
    }

    @Override
    public void onBorderSetWarningBlocks(WorldBorder border, int warningBlockDistance) {
        for (WorldBorderListenerOnce listener : this.delegate.keySet()) {
            listener.onBorderSetWarningBlocks(border, warningBlockDistance);
        }
        this.delegate.clear();
    }

    @Override
    public void onBorderSetDamagePerBlock(WorldBorder border, double damagePerBlock) {
        for (WorldBorderListenerOnce listener : this.delegate.keySet()) {
            listener.onBorderSetDamagePerBlock(border, damagePerBlock);
        }
        this.delegate.clear();
    }

    @Override
    public void onBorderSetDamageSafeZOne(WorldBorder border, double safeZoneRadius) {
        for (WorldBorderListenerOnce listener : this.delegate.keySet()) {
            listener.onBorderSetDamageSafeZOne(border, safeZoneRadius);
        }
        this.delegate.clear();
    }
}
