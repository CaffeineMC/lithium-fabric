package me.jellysquid.mods.lithium.common.cached_blockpos_iteration;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.util.math.BlockPos;

import java.util.Iterator;

/**
 * @author 2No2Name
 */
public class LongList2BlockPosMutableIterable implements Iterable<BlockPos> {

    private final LongList positions;
    private final int xOffset, yOffset, zOffset;

    public LongList2BlockPosMutableIterable(BlockPos offset, LongList posList) {
        this.xOffset = offset.getX();
        this.yOffset = offset.getY();
        this.zOffset = offset.getZ();
        this.positions = posList;
    }

    @Override
    public Iterator<BlockPos> iterator() {
        return new Iterator<BlockPos>() {

            private final LongIterator it = LongList2BlockPosMutableIterable.this.positions.iterator();
            private final BlockPos.Mutable pos = new BlockPos.Mutable();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public net.minecraft.util.math.BlockPos next() {
                long nextPos = this.it.nextLong();
                return this.pos.set(
                        LongList2BlockPosMutableIterable.this.xOffset + BlockPos.unpackLongX(nextPos),
                        LongList2BlockPosMutableIterable.this.yOffset + BlockPos.unpackLongY(nextPos),
                        LongList2BlockPosMutableIterable.this.zOffset + BlockPos.unpackLongZ(nextPos));
            }
        };
    }

}
