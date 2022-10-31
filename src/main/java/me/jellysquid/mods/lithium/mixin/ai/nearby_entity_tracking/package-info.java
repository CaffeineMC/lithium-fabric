@MixinConfigOption(
        description = """
                Event-based system for tracking nearby entities.
                """,
        depends = {
                @MixinConfigDependency(dependencyPath = "mixin.util.entity_section_position")
        }
)
package me.jellysquid.mods.lithium.mixin.ai.nearby_entity_tracking;

import net.caffeinemc.gradle.MixinConfigDependency;
import net.caffeinemc.gradle.MixinConfigOption;