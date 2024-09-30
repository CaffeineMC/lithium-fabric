@MixinConfigOption(
        description = "Send updates to hoppers when adding inventory block entities to chunks when world edit is loaded. " +
                "Fixes the issue of hoppers not noticing when inventories are placed using worldedit without any block updates. Enabled automatically when worldedit is present.",
        depends = @MixinConfigDependency(dependencyPath = "mixin.util.block_entity_retrieval"),
        enabled = false
)
package net.caffeinemc.mods.lithium.mixin.fabric.compat.worldedit;

import net.caffeinemc.gradle.MixinConfigDependency;
import net.caffeinemc.gradle.MixinConfigOption;

