@MixinConfigOption(
        description = "Allow hoppers to check whether a transfer-api inventory is present to avoid sleeping, which would prevent the hopper-inventory interaction. Enabled automatically when the transfer-api is present.",
        enabled = false
)
package net.caffeinemc.mods.lithium.mixin.fabric.compat.transfer_api;

import net.caffeinemc.gradle.MixinConfigOption;

