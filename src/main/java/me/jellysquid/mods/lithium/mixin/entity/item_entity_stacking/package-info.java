@MixinConfigOption(
        description = "Optimize item entity stacking by categorizing item entities by item type and only attempting to merge with the same type.",
        depends = {
                @MixinConfigDependency(dependencyPath = "mixin.util.accessors"),
                @MixinConfigDependency(dependencyPath = "mixin.util.item_stack_tracking")
        }
)
package me.jellysquid.mods.lithium.mixin.entity.item_entity_stacking;

import net.caffeinemc.gradle.MixinConfigDependency;
import net.caffeinemc.gradle.MixinConfigOption;