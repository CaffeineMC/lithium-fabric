package me.jellysquid.mods.lithium.common.blockentities;

public interface HopperCooldown {
	int cooldown();

	default boolean cooled() {
		return cooldown() <= 0;
	}
}
