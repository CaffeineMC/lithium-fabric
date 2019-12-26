package me.jellysquid.mods.lithium.mixin.redstone;

import me.jellysquid.mods.lithium.common.block.redstone.RedstoneEngine;
import me.jellysquid.mods.lithium.common.block.redstone.RedstoneLogic;
import me.jellysquid.mods.lithium.common.block.redstone.WorldWithRedstoneEngine;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(RedstoneWireBlock.class)
public abstract class MixinRedstoneWireBlock {
    /**
     * @reason Use insanely faster implementation
     * @author JellySquid
     */
    @Overwrite
    private BlockState update(World world, BlockPos pos, BlockState state) {
        if (world.isClient) {
            return state;
        }

        this.updateLogic(world, pos, state);

        RedstoneEngine engine = ((WorldWithRedstoneEngine) world).getRedstoneEngine();

        if (!engine.isUpdating()) {
            engine.flush();
        }

        return state;
    }

    private void updateLogic(World world, BlockPos pos, BlockState state) {
        RedstoneEngine engine = ((WorldWithRedstoneEngine) world).getRedstoneEngine();

        int power = engine.getWireCurrentPower(pos, state);
        int powerReceived = engine.getReceivedPower(pos);

        int powerContributed = 0;

        if (powerReceived < 15) {
            powerContributed = engine.getPowerContributed(pos);
        }

        int powerAdjusted = powerContributed - 1;

        if (powerReceived > powerAdjusted) {
            powerAdjusted = powerReceived;
        }

        if (power != powerAdjusted) {
            engine.setWireCurrentPower(pos, powerAdjusted);

            for (Vec3i offset : RedstoneLogic.WIRE_UPDATE_ORDER) {
                engine.enqueueNeighbor(pos.add(offset), Blocks.REDSTONE_WIRE, pos);
            }
        }

    }

}
