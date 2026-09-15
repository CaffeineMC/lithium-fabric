package net.caffeinemc.mods.lithium.neoforge;

import net.caffeinemc.mods.lithium.common.initialization.BlockInfoInitializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.datamaps.DataMapsUpdatedEvent;

@Mod(value = "lithium")
public class LithiumNeoForgeMod {
    public LithiumNeoForgeMod(IEventBus bus, ModContainer modContainer) {
        bus.addListener(DataMapsUpdatedEvent.class, event -> BlockInfoInitializer.resetBlockInfo());
    }
}