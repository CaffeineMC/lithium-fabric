@MixinConfigOption(
        description = "BlockEntity ticking caches whether the BlockEntity can exist in the BlockState at the same location." +
                " This deviates from vanilla in the case of placing a hopper in a powered location, immediately updating the" +
                " cached BlockState (which is incorrect in vanilla). This most likely does not affect your gameplay, as this" +
                " deviation only affects hoppers, and in vanilla, hoppers never use the cached state information anyway.",
        depends = @MixinConfigDependency(dependencyPath = "mixin.world.block_entity_ticking"),
        enabled = false //Lots of mod incompatibilities, TODO: analysis -> change the mixins or suggest changes to carpet & world edit
)
package me.jellysquid.mods.lithium.mixin.minimal_nonvanilla.world.block_entity_ticking.support_cache;

import net.caffeinemc.gradle.MixinConfigDependency;
import net.caffeinemc.gradle.MixinConfigOption;