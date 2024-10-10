@MixinConfigOption(
        description = "Uses faster block access for block collisions and delayed entity access with grouped boat/shulker for entity collisions when available",
        depends = {
                @MixinConfigDependency(dependencyPath = "mixin.util.chunk_access")
        }
)
package net.caffeinemc.mods.lithium.mixin.entity.collisions.intersection;

import net.caffeinemc.gradle.MixinConfigDependency;
import net.caffeinemc.gradle.MixinConfigOption;