@MixinConfigOption(description = "Use the block listening system to cache the entity suffocation check.",
depends = @MixinConfigDependency(dependencyPath = "mixin.util.block_tracking"))
package me.jellysquid.mods.lithium.mixin.experimental.entity.block_caching.suffocation;

import net.caffeinemc.gradle.MixinConfigDependency;
import net.caffeinemc.gradle.MixinConfigOption;