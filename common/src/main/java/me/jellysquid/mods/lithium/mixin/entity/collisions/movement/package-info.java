@MixinConfigOption(
        description = "Entity movement uses optimized block access and optimized and delayed entity access." +
                " Additionally, the supporting block of entities that only move downwards is checked first. This can" +
                " profit from mixin.experimental.entity.block_caching.block_support, but it is not required.",
        depends = @MixinConfigDependency(dependencyPath = "mixin.util.chunk_access")
)
package me.jellysquid.mods.lithium.mixin.entity.collisions.movement;

import net.caffeinemc.gradle.MixinConfigDependency;
import net.caffeinemc.gradle.MixinConfigOption;