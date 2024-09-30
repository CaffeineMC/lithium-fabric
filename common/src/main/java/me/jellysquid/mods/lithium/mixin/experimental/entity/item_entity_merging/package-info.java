@MixinConfigOption(
        description = "Optimize item entity merging by categorizing item entities by item type and only attempting to" +
                " merge with the same type. Categorizing by stack size allows skipping merge attempts of full item" +
                " entities or two more than half full item entities.",
        depends = {
                @MixinConfigDependency(dependencyPath = "mixin.util.accessors"),
                @MixinConfigDependency(dependencyPath = "mixin.util.entity_collection_replacement"),
                @MixinConfigDependency(dependencyPath = "mixin.util.item_component_and_count_tracking")
        }
)
package me.jellysquid.mods.lithium.mixin.experimental.entity.item_entity_merging;

import net.caffeinemc.gradle.MixinConfigDependency;
import net.caffeinemc.gradle.MixinConfigOption;