@MixinConfigOption(
        description = "Create game event dispatchers for chunk sections only when needed, i.e. when a" +
        " listener is added to a section. This reduces memory usage for chunks that do not have any listeners." +
        " The dispatchers are accessed more directly instead of indirectly through chunks." +
        " In total this speeds up attempting to dispatch events especially when there are no nearby listeners.",
        depends = @MixinConfigDependency(dependencyPath = "mixin.util.data_storage")
)
package me.jellysquid.mods.lithium.mixin.world.game_events.dispatch;

import net.caffeinemc.gradle.MixinConfigDependency;
import net.caffeinemc.gradle.MixinConfigOption;