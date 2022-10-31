/**
 * This package includes patches that store information about block states directly in the BlockState object to improve
 * the performance of accessing the FluidState of a BlockState or testing whether the BlockState is empty.
 */
@MixinConfigOption(description = "BlockStates store their FluidState directly and whether they are empty")
package me.jellysquid.mods.lithium.mixin.block.flatten_states;

import net.caffeinemc.gradle.MixinConfigOption;