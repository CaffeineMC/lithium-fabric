@MixinConfigOption(description = "Use block listening system to allow skipping stuff in entity code",
depends = @MixinConfigDependency(dependencyPath = "mixin.util.block_tracking"))
package net.caffeinemc.mods.lithium.mixin.experimental.entity.block_caching;

import net.caffeinemc.gradle.MixinConfigDependency;
import net.caffeinemc.gradle.MixinConfigOption;