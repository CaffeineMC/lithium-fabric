@MixinConfigOption(description = "Use the block listening system to cache entity fluid interaction when not touching fluid currents.",
depends = @MixinConfigDependency(dependencyPath = "mixin.util.block_tracking"))
package net.caffeinemc.mods.lithium.fabric.mixin.experimental.entity.block_caching.fluid_pushing;

import net.caffeinemc.gradle.MixinConfigDependency;
import net.caffeinemc.gradle.MixinConfigOption;