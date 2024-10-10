package net.caffeinemc.mods.lithium.common.entity.movement;

import com.google.common.collect.AbstractIterator;
import net.caffeinemc.mods.lithium.common.block.BlockCountingSection;
import net.caffeinemc.mods.lithium.common.block.BlockStateFlags;
import net.caffeinemc.mods.lithium.common.shapes.VoxelShapeCaster;
import net.caffeinemc.mods.lithium.common.util.Pos;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static net.caffeinemc.mods.lithium.common.entity.LithiumEntityCollisions.EPSILON;

/**
 * ChunkAwareBlockCollisionSweeper iterates over blocks in one chunk section at a time. Together with the chunk
 * section keeping track of the amount of oversized blocks inside the number of iterations can often be reduced.
 */
public class ChunkAwareBlockCollisionSweeper extends AbstractIterator<VoxelShape> {

    private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    /**
     * The collision box being swept through the world.
     */
    private final AABB box;

    /**
     * The VoxelShape of the collision box being swept through the world.
     */
    private final VoxelShape shape;

    private final Level world;

    private final CollisionContext context;

    //limits of the area without extension for oversized blocks
    private final int minX, minY, minZ, maxX, maxY, maxZ;

    //variables prefixed with c refer to the iteration of the currently cached chunk section
    private int chunkX, chunkYIndex, chunkZ;
    private int cStartX, cStartZ;
    private int cEndX, cEndZ;
    private int cX, cY, cZ;
    
    private int maxHitX;
    private int maxHitY;
    private int maxHitZ;
    private VoxelShape maxShape;
    private final boolean hideLastCollision;


    private int cTotalSize;
    private int cIterated;

    private boolean sectionOversizedBlocks;
    private ChunkAccess cachedChunk;
    private LevelChunkSection cachedChunkSection;

    public ChunkAwareBlockCollisionSweeper(Level world, @Nullable Entity entity, AABB box) {
        this(world, entity, box, false);
    }
    public ChunkAwareBlockCollisionSweeper(Level world, @Nullable Entity entity, AABB box, boolean hideLastCollision) {
        this.box = box;
        this.shape = Shapes.create(box);
        this.context = entity == null ? CollisionContext.empty() : CollisionContext.of(entity);
        this.world = world;

        this.minX = Mth.floor(box.minX - EPSILON);
        this.maxX = Mth.floor(box.maxX + EPSILON);
        this.minY = Mth.clamp(Mth.floor(box.minY - EPSILON), Pos.BlockCoord.getMinY(this.world), Pos.BlockCoord.getMaxYInclusive(this.world));
        this.maxY = Mth.clamp(Mth.floor(box.maxY + EPSILON), Pos.BlockCoord.getMinY(this.world), Pos.BlockCoord.getMaxYInclusive(this.world));
        this.minZ = Mth.floor(box.minZ - EPSILON);
        this.maxZ = Mth.floor(box.maxZ + EPSILON);

        this.chunkX = Pos.ChunkCoord.fromBlockCoord(expandMin(this.minX));
        this.chunkZ = Pos.ChunkCoord.fromBlockCoord(expandMin(this.minZ));

        this.cIterated = 0;
        this.cTotalSize = 0;

        this.maxHitX = Integer.MIN_VALUE;
        this.maxHitY = Integer.MIN_VALUE;
        this.maxHitZ = Integer.MIN_VALUE;
        this.maxShape = null;
        this.hideLastCollision = hideLastCollision;

        //decrement as first nextSection call will increment it again
        this.chunkX--;
    }


    public VoxelShape getLastCollision() {
        return this.maxShape;
    }

    public Iterator<VoxelShape> getLastCollisionIterator() {
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return hideLastCollision && maxShape != null;
            }

            @Override
            public VoxelShape next() {
                if (this.hasNext()) {
                    VoxelShape previousMaxShape = maxShape;
                    maxShape = null;
                    return previousMaxShape;
                }
                throw new NoSuchElementException();
            }
        };
    }

    private boolean nextSection() {
        do {
            do {
                //find the coordinates of the next section inside the area expanded by 1 block on all sides
                //note: this.minX, maxX etc are not expanded, so there are lots of +1 and -1 around.
                if (
                        this.cachedChunk != null &&
                        this.chunkYIndex < Pos.SectionYIndex.getMaxYSectionIndexInclusive(this.world) &&
                        this.chunkYIndex < Pos.SectionYIndex.fromBlockCoord(this.world,expandMax(this.maxY))
                ) {
                    this.chunkYIndex++;
                    this.cachedChunkSection = this.cachedChunk.getSections()[this.chunkYIndex];
                } else {
                    this.chunkYIndex = Mth.clamp(
                            Pos.SectionYIndex.fromBlockCoord(this.world, expandMin(this.minY)),
                            Pos.SectionYIndex.getMinYSectionIndex(this.world),
                            Pos.SectionYIndex.getMaxYSectionIndexInclusive(this.world)
                    );

                    if (this.chunkX < Pos.ChunkCoord.fromBlockCoord(expandMax(this.maxX))) {
                        //first initialization takes this branch
                        this.chunkX++;
                    } else {
                        this.chunkX = Pos.ChunkCoord.fromBlockCoord(expandMin(this.minX));

                        if (this.chunkZ < Pos.ChunkCoord.fromBlockCoord(expandMax(this.maxZ))) {
                            this.chunkZ++;
                        } else {
                            return false; //no more sections to iterate
                        }
                    }
                    this.cachedChunk = this.world.getChunk(this.chunkX, this.chunkZ, ChunkStatus.FULL, false);
                    if (this.cachedChunk != null) {
                        this.cachedChunkSection = this.cachedChunk.getSections()[this.chunkYIndex];
                    }
                }
                //skip empty chunks and empty chunk sections
            } while (this.cachedChunk == null || this.cachedChunkSection == null || this.cachedChunkSection.hasOnlyAir());

            this.sectionOversizedBlocks = hasChunkSectionOversizedBlocks(this.cachedChunk, this.chunkYIndex);

            int sizeExtension = this.sectionOversizedBlocks ? 1 : 0;

            this.cEndX = Math.min(this.maxX + sizeExtension, Pos.BlockCoord.getMaxInSectionCoord(this.chunkX));
            int cEndY = Math.min(this.maxY + sizeExtension, Pos.BlockCoord.getMaxYInSectionIndex(this.world, this.chunkYIndex));
            this.cEndZ = Math.min(this.maxZ + sizeExtension, Pos.BlockCoord.getMaxInSectionCoord(this.chunkZ));

            this.cStartX = Math.max(this.minX - sizeExtension, Pos.BlockCoord.getMinInSectionCoord(this.chunkX));
            int cStartY = Math.max(this.minY - sizeExtension, Pos.BlockCoord.getMinYInSectionIndex(this.world, this.chunkYIndex));
            this.cStartZ = Math.max(this.minZ - sizeExtension, Pos.BlockCoord.getMinInSectionCoord(this.chunkZ));
            this.cX = this.cStartX;
            this.cY = cStartY;
            this.cZ = this.cStartZ;

            this.cTotalSize = (this.cEndX - this.cStartX + 1) * (cEndY - cStartY + 1) * (this.cEndZ - this.cStartZ + 1);
            //skip completely empty section iterations
        } while (this.cTotalSize == 0);
        this.cIterated = 0;

        return true;
    }

    /**
     * Advances the sweep forward until finding a block with a box-colliding VoxelShape
     *
     * @return null if no VoxelShape is left in the area, otherwise the next VoxelShape
     */
    @Override
    public VoxelShape computeNext() {
        while (true) {
            if (this.cIterated >= this.cTotalSize) {
                if (!this.nextSection()) {
                    break;
                }
            }

            this.cIterated++;


            final int x = this.cX;
            final int y = this.cY;
            final int z = this.cZ;

            //The iteration order within a chunk section is chosen so that it causes a mostly linear array access in the storage.
            //In net.minecraft.world.chunk.PalettedContainer.toIndex x gets the 4 least significant bits, z the 4 above, and y the 4 even higher ones.
            //Linearly accessing arrays is faster than other access patterns.
            if (this.cX < this.cEndX) {
                this.cX++;
            } else if (this.cZ < this.cEndZ) {
                this.cX = this.cStartX;
                this.cZ++;
            } else {
                this.cX = this.cStartX;
                this.cZ = this.cStartZ;
                this.cY++;
                //stop condition was already checked using this.cIterated at the start of the method
            }

            //using < minX and > maxX instead of <= and >= in vanilla, because minX, maxX are the coordinates
            //of the box that wasn't extended for oversized blocks yet.
            final int edgesHit = this.sectionOversizedBlocks ?
                    (x < this.minX || x > this.maxX ? 1 : 0) +
                            (y < this.minY || y > this.maxY ? 1 : 0) +
                            (z < this.minZ || z > this.maxZ ? 1 : 0) : 0;

            if (edgesHit == 3) {
                continue;
            }

            final BlockState state = this.cachedChunkSection.getBlockState(x & 15, y & 15, z & 15);

            if (!canInteractWithBlock(state, edgesHit)) {
                continue;
            }

            this.pos.set(x, y, z);

            VoxelShape collisionShape = state.getCollisionShape(this.world, this.pos, this.context);

            if (collisionShape != Shapes.empty() && collisionShape != null /*collisionShape should never be null, but we received crash reports.*/) {
                VoxelShape collidedShape = getCollidedShape(this.box, this.shape, collisionShape, x, y, z);
                if (collidedShape != null) {
                    if (z >= this.maxHitZ && (z > this.maxHitZ || y >= this.maxHitY && (y > this.maxHitY || x > this.maxHitX))) {
                        this.maxHitX = x;
                        this.maxHitY = y;
                        this.maxHitZ = z;
                        //Always make sure the shape at the maximum position is the last one returned, because
                        // the last shape has a different 1e-7 behavior (no next shape that clips movement to 0).
                        // This does affect certain contraptions: https://github.com/CaffeineMC/lithium-fabric/issues/443
                        VoxelShape previousMaxShape = this.maxShape;
                        this.maxShape = collidedShape;
                        if (previousMaxShape != null) {
                            return previousMaxShape;
                        }
                    } else {
                        return collidedShape;
                    }
                }
            }
        }

        if (!this.hideLastCollision && this.maxShape != null) {
            VoxelShape previousMaxShape = this.maxShape;
            this.maxShape = null;
            return previousMaxShape;
        }

        return this.endOfData();
    }

    /**
     * This is an artifact from vanilla which is used to avoid testing shapes in the extended portion of a volume
     * unless they are a shape which exceeds their voxel. Pistons must be special-cased here.
     *
     * @return True if the shape can be interacted with at the given edge boundary
     */
    private static boolean canInteractWithBlock(BlockState state, int edgesHit) {
        return (edgesHit != 1 || state.hasLargeCollisionShape()) && (edgesHit != 2 || state.getBlock() == Blocks.MOVING_PISTON);
    }

    /**
     * Checks if the {@param entityShape} or {@param entityBox} intersects the given {@param shape} which is translated
     * to the given position. This is a very specialized implementation which tries to avoid going through VoxelShape
     * for full-cube shapes.
     *
     * @return A {@link VoxelShape} which contains the shape representing that which was collided with, otherwise null
     */
    private static VoxelShape getCollidedShape(AABB entityBox, VoxelShape entityShape, VoxelShape shape, int x, int y, int z) {
        if (shape == Shapes.block()) {
            return entityBox.intersects(x, y, z, x + 1.0, y + 1.0, z + 1.0) ? shape.move(x, y, z) : null;
        }
        if (shape instanceof VoxelShapeCaster) {
            if (((VoxelShapeCaster) shape).intersects(entityBox, x, y, z)) {
                return shape.move(x, y, z);
            } else {
                return null;
            }
        }

        shape = shape.move(x, y, z);

        if (Shapes.joinIsNotEmpty(shape, entityShape, BooleanOp.AND)) {
            return shape;
        }

        return null;
    }

    private static int expandMin(int coord) {
        return coord - 1;
    }
    private static int expandMax(int coord) {
        return coord + 1;
    }

    /**
     * Checks the cached information whether the {@param chunkY} section of the {@param chunk} has oversized blocks.
     *
     * @return Whether there are any oversized blocks in the chunk section.
     */
    private static boolean hasChunkSectionOversizedBlocks(ChunkAccess chunk, int chunkY) {
        if (BlockStateFlags.ENABLED) {
            LevelChunkSection section = chunk.getSections()[chunkY];
            return section != null && ((BlockCountingSection) section).lithium$mayContainAny(BlockStateFlags.OVERSIZED_SHAPE);
        }
        return true; //like vanilla, assume that a chunk section has oversized blocks, when the section mixin isn't loaded
    }

    public List<VoxelShape> collectAll() {
        ArrayList<VoxelShape> collisions = new ArrayList<>();

        while (this.hasNext()) {
            collisions.add(this.next());
        }

        return collisions;
    }
}
