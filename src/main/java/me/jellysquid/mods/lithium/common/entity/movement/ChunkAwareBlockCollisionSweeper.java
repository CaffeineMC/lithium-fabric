package me.jellysquid.mods.lithium.common.entity.movement;

import me.jellysquid.mods.lithium.common.entity.LithiumEntityCollisions;
import me.jellysquid.mods.lithium.common.shapes.VoxelShapeCaster;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.CollisionView;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;

import static me.jellysquid.mods.lithium.common.entity.LithiumEntityCollisions.EPSILON;

/**
 * ChunkAwareBlockCollisionSweeper iterates over blocks in one chunk section at a time. Together with the chunk
 * section keeping track of the amount of oversized blocks inside the number of iterations can often be reduced.
 */
public class ChunkAwareBlockCollisionSweeper {
    private static final boolean OVERSIZED_BLOCK_COUNTING_ENABLED = OversizedBlocksCounter.class.isAssignableFrom(ChunkSection.class);

    private final BlockPos.Mutable pos = new BlockPos.Mutable();

    /**
     * The collision box being swept through the world.
     */
    private final Box box;

    /**
     * The VoxelShape of the collision box being swept through the world.
     */
    private final VoxelShape shape;

    private final CollisionView view;

    private final ShapeContext context;

    private final BlockCollisionPredicate collisionPredicate;

    private final Entity entity;

    //limits of the area without extension for oversized blocks
    private final int minX, minY, minZ, maxX, maxY, maxZ;

    //variables prefixed with c refer to the iteration of the currently cached chunk section
    private int chunkX, chunkY, chunkZ;
    private int cStartX, cStartZ;
    private int cEndX, cEndZ;
    private int cX, cY, cZ;

    private int cTotalSize;
    private int cIterated;

    private boolean sectionOversizedBlocks;
    private Chunk cachedChunk;
    private ChunkSection cachedChunkSection;
    private boolean needEntityCollisionCheck;

    public ChunkAwareBlockCollisionSweeper(CollisionView view, Entity entity, Box box, BlockCollisionPredicate collisionPredicate) {
        this.box = box;
        this.shape = VoxelShapes.cuboid(box);
        this.context = entity == null ? ShapeContext.absent() : ShapeContext.of(entity);
        this.view = view;
        this.entity = entity;
        this.needEntityCollisionCheck = entity != null;

        this.minX = MathHelper.floor(box.minX - EPSILON);
        this.maxX = MathHelper.floor(box.maxX + EPSILON);
        this.minY = MathHelper.clamp((int) (box.minY - EPSILON), 0, 255);
        this.maxY = MathHelper.clamp((int) (box.maxY + EPSILON), 0, 255);
        this.minZ = MathHelper.floor(box.minZ - EPSILON);
        this.maxZ = MathHelper.floor(box.maxZ + EPSILON);
        this.collisionPredicate = collisionPredicate;

        this.chunkX = (this.minX - 1) >> 4;
        this.chunkZ = (this.minZ - 1) >> 4;

        this.cIterated = 0;
        this.cTotalSize = 0;

        //decrement as first nextSection call will increment it again
        this.chunkX--;
    }

    private boolean nextSection() {
        do {
            do {
                //find the coordinates of the next section inside the area expanded by 1 block on all sides
                //note: this.minX, maxX etc are not expanded, so there are lots of +1 and -1 around.
                if (this.cachedChunk != null && this.chunkY < 15 && this.chunkY < ((this.maxY + 1) >> 4)) {
                    this.chunkY++;
                    this.cachedChunkSection = this.cachedChunk.getSectionArray()[this.chunkY];
                } else {
                    this.chunkY = MathHelper.clamp((this.minY - 1) >> 4, 0, 15);

                    if ((this.chunkX < ((this.maxX + 1) >> 4))) {
                        //first initialization takes this branch
                        this.chunkX++;
                    } else {
                        this.chunkX = (this.minX - 1) >> 4;

                        if (this.chunkZ < ((this.maxZ + 1) >> 4)) {
                            this.chunkZ++;
                        } else {
                            return false; //no more sections to iterate
                        }
                    }
                    //Casting to Chunk is not checked, together with other mods this could cause a ClassCastException
                    this.cachedChunk = (Chunk) this.view.getChunkAsView(this.chunkX, this.chunkZ);
                    if (this.cachedChunk != null) {
                        this.cachedChunkSection = this.cachedChunk.getSectionArray()[this.chunkY];
                    }
                }
                //skip empty chunks and empty chunk sections
            } while (this.cachedChunk == null || ChunkSection.isEmpty(this.cachedChunkSection));

            this.sectionOversizedBlocks = hasChunkSectionOversizedBlocks(this.cachedChunk, this.chunkY);

            int sizeExtension = this.sectionOversizedBlocks ? 1 : 0;

            this.cEndX = Math.min(this.maxX + sizeExtension, 15 + (this.chunkX << 4));
            int cEndY = Math.min(this.maxY + sizeExtension, 15 + (this.chunkY << 4));
            this.cEndZ = Math.min(this.maxZ + sizeExtension, 15 + (this.chunkZ << 4));

            this.cStartX = Math.max(this.minX - sizeExtension, this.chunkX << 4);
            int cStartY = Math.max(this.minY - sizeExtension, this.chunkY << 4);
            this.cStartZ = Math.max(this.minZ - sizeExtension, this.chunkZ << 4);
            this.cX = this.cStartX;
            this.cY = cStartY;
            this.cZ = this.cStartZ;

            this.cTotalSize = (this.cEndX - this.cStartX + 1) * (cEndY - cStartY + 1) * (this.cEndZ - this.cStartZ + 1);
            //skip completely empty section iterations
        } while (this.cTotalSize == 0);
        this.cIterated = 0;

        return true;
    }

    public VoxelShape getNextCollidedShape() {
        VoxelShape shape = null;

        if (this.needEntityCollisionCheck) {
            shape = this.getNextEntityCollision();

            this.needEntityCollisionCheck = false;
        }

        if (shape == null) {
            shape = this.getNextBlockCollision();
        }

        return shape;
    }

    /**
     * Advances the sweep forward until finding a block with a box-colliding VoxelShape
     *
     * @return null if no VoxelShape is left in the area, otherwise the next VoxelShape
     */
    private VoxelShape getNextBlockCollision() {
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
            //Linearly accessing arrays might be slightly faster than other access patterns.
            //This code hasn't been benchmarked in comparison to another access order.
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

            if (!this.collisionPredicate.test(this.view, this.pos, state)) {
                continue;
            }

            VoxelShape collisionShape = state.getCollisionShape(this.view, this.pos, this.context);

            if (collisionShape != VoxelShapes.empty()) {
                VoxelShape collidedShape = getCollidedShape(this.box, this.shape, collisionShape, x, y, z);
                if (collidedShape != null) {
                    return collidedShape;
                }
            }
        }

        return null;
    }

    private VoxelShape getNextEntityCollision() {
        if (LithiumEntityCollisions.canEntityCollideWithWorldBorder(this.view, this.entity)) {
            return this.view.getWorldBorder().asVoxelShape();
        }

        return null;
    }

    /**
     * This is an artifact from vanilla which is used to avoid testing shapes in the extended portion of a volume
     * unless they are a shape which exceeds their voxel. Pistons must be special-cased here.
     *
     * @return True if the shape can be interacted with at the given edge boundary
     */
    private static boolean canInteractWithBlock(BlockState state, int edgesHit) {
        return (edgesHit != 1 || state.exceedsCube()) && (edgesHit != 2 || state.getBlock() == Blocks.MOVING_PISTON);
    }

    /**
     * Checks if the {@param entityShape} or {@param entityBox} intersects the given {@param shape} which is translated
     * to the given position. This is a very specialized implementation which tries to avoid going through VoxelShape
     * for full-cube shapes.
     *
     * @return A {@link VoxelShape} which contains the shape representing that which was collided with, otherwise null
     */
    private static VoxelShape getCollidedShape(Box entityBox, VoxelShape entityShape, VoxelShape shape, int x, int y, int z) {
        if (shape instanceof VoxelShapeCaster) {
            if (((VoxelShapeCaster) shape).intersects(entityBox, x, y, z)) {
                return shape.offset(x, y, z);
            } else {
                return null;
            }
        }

        shape = shape.offset(x, y, z);

        if (VoxelShapes.matchesAnywhere(shape, entityShape, BooleanBiFunction.AND)) {
            return shape;
        }

        return null;
    }

    /**
     * Checks the cached information whether the {@param chunkY} section of the {@param chunk} has oversized blocks.
     *
     * @return Whether there are any oversized blocks in the chunk section.
     */
    private static boolean hasChunkSectionOversizedBlocks(Chunk chunk, int chunkY) {
        if (OVERSIZED_BLOCK_COUNTING_ENABLED) {
            ChunkSection section = chunk.getSectionArray()[chunkY];
            return section != null && ((OversizedBlocksCounter) section).hasOversizedBlocks();
        }
        return true; //like vanilla, assume that a chunk section has oversized blocks, when the section mixin isn't loaded
    }

    public interface OversizedBlocksCounter {
        boolean hasOversizedBlocks();
    }
}
