@MixinConfigOption(description = "Allow grouping entity classes for faster entity access, e.g. boats and shulkers",
depends = @MixinConfigDependency(dependencyPath = "mixin.util.accessors"))
package net.caffeinemc.mods.lithium.mixin.chunk.entity_class_groups;

import net.caffeinemc.gradle.MixinConfigDependency;
import net.caffeinemc.gradle.MixinConfigOption;