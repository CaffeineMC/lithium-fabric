@MixinConfigOption(
        description = "Speed up finding empty spaces mobs fit into. This speeds up entity pose checks and nether portal" +
                " positioning for colliding mobs (This code is vanilla's nether portal horse suffocation fix)." +
                " If certain block collision surfaces have coordinates that are different but within 1e-7 of each other," +
                " this optimization may cause entities coming from nether portals or changing pose to be placed in a" +
                " different position or pose than vanilla. This effect only occurs when the decision whether the entity" +
                " fits into a space depends on a difference in the magnitude of 1e-7 blocks."
)
package me.jellysquid.mods.lithium.mixin.minimal_nonvanilla.collisions.empty_space;

import net.caffeinemc.gradle.MixinConfigOption;