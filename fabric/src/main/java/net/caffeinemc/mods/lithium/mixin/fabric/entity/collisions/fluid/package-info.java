@MixinConfigOption(
        description = "Skips being pushed by fluids when the nearby chunk sections do not contain this fluid",
        depends = {
                @MixinConfigDependency(dependencyPath = "mixin.util.block_tracking"),
                @MixinConfigDependency(dependencyPath = "mixin.experimental.entity.block_caching.fluid_pushing", enabled = false)
        }
)
package net.caffeinemc.mods.lithium.mixin.fabric.entity.collisions.fluid;

import net.caffeinemc.gradle.MixinConfigDependency;
import net.caffeinemc.gradle.MixinConfigOption;