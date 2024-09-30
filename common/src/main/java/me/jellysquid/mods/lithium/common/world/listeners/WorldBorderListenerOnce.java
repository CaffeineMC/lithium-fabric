package me.jellysquid.mods.lithium.common.world.listeners;

import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;

public interface WorldBorderListenerOnce extends BorderChangeListener {

    void lithium$onWorldBorderShapeChange(WorldBorder worldBorder);

    default void onAreaReplaced(WorldBorder border) {
        this.lithium$onWorldBorderShapeChange(border);
    }

    @Override
    default void onBorderSizeSet(WorldBorder border, double size) {
        this.lithium$onWorldBorderShapeChange(border);
    }

    @Override
    default void onBorderSizeLerping(WorldBorder border, double fromSize, double toSize, long time) {
        this.lithium$onWorldBorderShapeChange(border);
    }

    @Override
    default void onBorderCenterSet(WorldBorder border, double centerX, double centerZ) {
        this.lithium$onWorldBorderShapeChange(border);
    }

    @Override
    default void onBorderSetWarningTime(WorldBorder border, int warningTime) {

    }

    @Override
    default void onBorderSetWarningBlocks(WorldBorder border, int warningBlockDistance) {

    }

    @Override
    default void onBorderSetDamagePerBlock(WorldBorder border, double damagePerBlock) {

    }

    @Override
    default void onBorderSetDamageSafeZOne(WorldBorder border, double safeZoneRadius) {

    }
}
