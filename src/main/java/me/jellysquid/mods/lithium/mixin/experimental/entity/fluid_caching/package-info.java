@MixinConfigOption(description = "Use block listening system to allow skipping entity fluid current checks when the entity is not touching the respective fluid",
depends = @MixinConfigDependency(dependencyPath = "mixin.util.block_tracking.block_listening"))
package me.jellysquid.mods.lithium.mixin.experimental.entity.fluid_caching;

import net.caffeinemc.gradle.MixinConfigDependency;
import net.caffeinemc.gradle.MixinConfigOption;