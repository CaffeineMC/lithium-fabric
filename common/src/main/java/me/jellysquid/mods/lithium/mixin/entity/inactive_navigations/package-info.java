@MixinConfigOption(
        description = "Block updates skip notifying mobs that won't react to the block update anyways",
        depends = @MixinConfigDependency(dependencyPath = "mixin.util.data_storage")
)
package me.jellysquid.mods.lithium.mixin.entity.inactive_navigations;

import net.caffeinemc.gradle.MixinConfigDependency;
import net.caffeinemc.gradle.MixinConfigOption;