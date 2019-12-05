package me.jellysquid.mods.lithium.mixin.no_debug_world_type;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(WorldChunk.class)
public class MixinWorldChunk {
    @Shadow
    @Final
    private ChunkSection[] sections;

    /**
     * @reason Remove the debug world check
     * @author JellySquid
     */
    @Overwrite
    public BlockState getBlockState(BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        try {
            int chunkY = y >> 4;

            if (chunkY >= 0 && chunkY < this.sections.length) {
                ChunkSection section = this.sections[chunkY];

                if (section != null) {
                    return section.getBlockState(x & 15, y & 15, z & 15);
                }
            }

            return Blocks.AIR.getDefaultState();
        } catch (Throwable e) {
            throw onGetBlockStateException(e, x, y, z);
        }
    }

    private static CrashException onGetBlockStateException(Throwable cause, int x, int y, int z) {
        CrashReport report = CrashReport.create(cause, "Getting block state");
        CrashReportSection section = report.addElement("Block being got");
        section.add("Location", () -> CrashReportSection.createPositionString(x, y, z));
        return new CrashException(report);
    }
}
