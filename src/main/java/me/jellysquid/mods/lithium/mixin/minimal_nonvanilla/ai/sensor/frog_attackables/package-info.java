@MixinConfigOption(
        description = "Speed up frog attackable sensor by checking entity type before visibility test." +
        " This is slightly non-vanilla because the visibility information is cached for up to a second." +
        " If this sensor does not compute the visibility test, a later access might compute the visibility instead." +
        " That can cause a different result, since the later computation leads to a more updated result."
)
package me.jellysquid.mods.lithium.mixin.minimal_nonvanilla.ai.sensor.frog_attackables;

import net.caffeinemc.gradle.MixinConfigOption;