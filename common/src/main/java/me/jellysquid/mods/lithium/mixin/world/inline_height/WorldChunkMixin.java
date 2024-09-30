package me.jellysquid.mods.lithium.mixin.world.inline_height;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LevelChunk.class)
public abstract class WorldChunkMixin implements LevelHeightAccessor {

    @Shadow
    @Final
    Level level;

    @Override
    public int getMaxBuildHeight() {
        return this.level.getMaxBuildHeight();
    }

    @Override
    public int getSectionsCount() {
        return this.level.getSectionsCount();
    }

    @Override
    public int getMinSection() {
        return this.level.getMinSection();
    }

    @Override
    public int getMaxSection() {
        return this.level.getMaxSection();
    }

    @Override
    public boolean isOutsideBuildHeight(BlockPos pos) {
        return this.level.isOutsideBuildHeight(pos);
    }

    @Override
    public boolean isOutsideBuildHeight(int y) {
        return this.level.isOutsideBuildHeight(y);
    }

    @Override
    public int getSectionIndex(int y) {
        return this.level.getSectionIndex(y);
    }

    @Override
    public int getSectionIndexFromSectionY(int coord) {
        return this.level.getSectionIndexFromSectionY(coord);
    }

    @Override
    public int getSectionYFromSectionIndex(int index) {
        return this.level.getSectionYFromSectionIndex(index);
    }
}
