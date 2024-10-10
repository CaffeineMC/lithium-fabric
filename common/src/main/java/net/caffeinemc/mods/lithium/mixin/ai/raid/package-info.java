@MixinConfigOption(
        description = "Avoids unnecessary raid bar updates and optimizes expensive leader banner operations",
        depends = @MixinConfigDependency(dependencyPath = "mixin.util.data_storage")
)
package net.caffeinemc.mods.lithium.mixin.ai.raid;

import net.caffeinemc.gradle.MixinConfigDependency;
import net.caffeinemc.gradle.MixinConfigOption;