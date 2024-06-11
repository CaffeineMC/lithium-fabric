@MixinConfigOption(
        description = "Create game event dispatchers for chunk sections only when needed, i.e. when a" +
        " listener is added to a section. This reduces memory usage for chunks that do not have any listeners." +
        " This speeds up attempting to dispatch events when there are no nearby listeners, which could be sculk" +
        " sensors, allays, wardens or sculk shriekers."
)
package me.jellysquid.mods.lithium.mixin.world.game_events.dispatch_to_empty;

import net.caffeinemc.gradle.MixinConfigOption;