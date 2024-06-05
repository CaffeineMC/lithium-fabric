@MixinConfigOption(description = "Disable the parent animal sensor when an animal is not a baby." +
        " Would differ from vanilla in the case where an adult animal turns back into a baby animal, as the sensor information is refreshed," +
        " leading to a less-outdated value in the first second of turning back into a baby animal. However, there is no way to turn an animal" +
        " back into a baby without reinitializing the brain, creating entirely new sensors."
)
package me.jellysquid.mods.lithium.mixin.ai.useless_sensors.parent_animal_sensor;

import net.caffeinemc.gradle.MixinConfigOption;