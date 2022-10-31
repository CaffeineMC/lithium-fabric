@MixinConfigOption(
        description = "Uses faster block access for block collisions and delayed entity access with grouped boat/shulker for entity collisions when available",
        depends = {
                @MixinConfigDependency(dependencyPath = "mixin.chunk.block_counting")
        }
)
package me.jellysquid.mods.lithium.mixin.entity.collisions.intersection;

import net.caffeinemc.gradle.MixinConfigDependency;
import net.caffeinemc.gradle.MixinConfigOption;