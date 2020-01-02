package me.jellysquid.mods.lithium.common.shapes;

import me.jellysquid.mods.lithium.common.debug.CollisionTracer;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Implements a box sweeping algorithm which is used to find all voxels a bounding box will intersect with given a
 * vector. This works by casting a ray from the "leading" corner of the specified bounding box, which is then used to
 * traverse along a voxel grid as {@link BoxSweeper#step()} is continuously called until completion. When an edge is
 * crossed on the voxel grid by the ray, we iterate over the new voxels the box is intersecting (based on the axis of
 * intersection) and then notify the listening iterator.
 *
 * This makes use of the algorithm described in the paper "A Fast Voxel Traversal Algorithm for Ray-Tracing":
 * http://www.cse.yorku.ca/~amana/research/grid.pdf
 *
 * Many of the variables names closely match those described in the paper.
 */
public class BoxSweeper {
    private static final double EPSILON = 1.0E-7D;

    // The iterator which will be notified when voxels have been traversed
    private final SweepIterator iterator;

    // The length of the ray we are iterating along
    private final double maxDistance;

    // The distance that must be moved on each axis to cross into the next voxel
    private final double tDeltaX, tDeltaY, tDeltaZ;

    // The normalized motion vector
    private final double normX, normY, normZ;

    // The current distance travelled so far along the ray
    private double distance;

    // The advancement which will be made on each axis when crossing an edge
    private final int stepX, stepY, stepZ;

    // The value of "t" at which the ray crosses the next voxel boundary
    private double tMaxX, tMaxY, tMaxZ;

    // The leading edge coordinates
    private double ldX, ldY, ldZ;

    // The trailing edge coordinates
    private double trX, trY, trZ;

    // The integer component of the leading edge coordinates
    private int ldiX, ldiY, ldiZ;

    // The integer component of the trailing edge coordinates
    private int triX, triY, triZ;

    // The most recently crossed axis
    // Initialized to null so the first step iteration will add all blocks the box is initially occupying
    private Axis axis = null;

    /**
     * @param box The bounding box to sweep through the voxel grid
     * @param motion The motion vector of the bounding box
     * @param voxelExtentLimit The maximum size of any voxel in the world
     * @param iterator The iterator which is called for every voxel intersected during a step
     */
    public BoxSweeper(Box box, Vec3d motion, double voxelExtentLimit, SweepIterator iterator) {
        if (voxelExtentLimit < 1.0D) {
            throw new IllegalArgumentException("The voxel extent limit must be at least 1.0");
        }

        double excess = voxelExtentLimit - 1.0D;

        this.iterator = iterator;

        boolean dirX = motion.x >= 0.0D;
        boolean dirY = motion.y >= 0.0D;
        boolean dirZ = motion.z >= 0.0D;

        this.maxDistance = motion.length();

        this.normX = motion.x / this.maxDistance;
        this.normY = motion.y / this.maxDistance;
        this.normZ = motion.z / this.maxDistance;

        this.stepX = dirX ? 1 : -1;
        this.stepY = dirY ? 1 : -1;
        this.stepZ = dirZ ? 1 : -1;

        this.ldX = (dirX ? box.x2 + excess : box.x1 - excess);
        this.ldY = (dirY ? box.y2 + excess : box.y1 - excess);
        this.ldZ = (dirZ ? box.z2 + excess : box.z1 - excess);

        this.trX = (dirX ? box.x1 - excess : box.x2 + excess);
        this.trY = (dirY ? box.y1 - excess : box.y2 + excess);
        this.trZ = (dirZ ? box.z1 - excess : box.z2 + excess);

        this.ldiX = getLeadingEdgeToVoxel(this.ldX, this.stepX);
        this.ldiY = getLeadingEdgeToVoxel(this.ldY, this.stepY);
        this.ldiZ = getLeadingEdgeToVoxel(this.ldZ, this.stepZ);

        this.triX = getTrailingEdgeToVoxel(this.trX, this.stepX);
        this.triY = getTrailingEdgeToVoxel(this.trY, this.stepY);
        this.triZ = getTrailingEdgeToVoxel(this.trZ, this.stepZ);

        this.tDeltaX = getDistanceToNextVoxel(this.normX);
        this.tDeltaY = getDistanceToNextVoxel(this.normY);
        this.tDeltaZ = getDistanceToNextVoxel(this.normZ);

        this.tMaxX = getNextVoxelCoordinate(this.tDeltaX, this.ldX, this.ldiX, dirX);
        this.tMaxY = getNextVoxelCoordinate(this.tDeltaY, this.ldY, this.ldiY, dirY);
        this.tMaxZ = getNextVoxelCoordinate(this.tDeltaZ, this.ldZ, this.ldiZ, dirZ);

        CollisionTracer.IMPL.addTracedRay(this.ldX, this.ldY, this.ldZ, this.ldX + motion.x, this.ldY + motion.y, this.ldZ + motion.z);
    }

    /**
     * Performs the next step of the sweeping algorithm, notifying the {@link SweepIterator} of any newly entered
     * voxels as they are crossed in the step.
     *
     * @return True if there are voxels remaining to be stepped through, otherwise false.
     */
    public boolean next() {
        // There's nothing to sweep!
        if (this.maxDistance == 0.0D) {
            return false;
        }

        if (this.distance <= this.maxDistance) {
            this.checkVoxels(this.axis);
            this.step();

            return true;
        }

        return false;
    }

    private void step() {
        if (this.tMaxX < this.tMaxY) {
            if (this.tMaxX < this.tMaxZ) {
                this.stepX();
            } else {
                this.stepZ();
            }
        } else {
            if (this.tMaxY < this.tMaxZ) {
                this.stepY();
            } else {
                this.stepZ();
            }
        }
    }

    private void stepX() {
        double dt = this.tMaxX - this.distance;

        this.distance = this.tMaxX;

        this.ldiX += this.stepX;
        this.tMaxX += this.tDeltaX;

        this.trX += dt * this.normX;
        this.triX = getTrailingEdgeToVoxel(this.trX, this.stepX);

        this.axis = Axis.X;
    }

    private void stepY() {
        double dt = this.tMaxY - this.distance;

        this.distance = this.tMaxY;

        this.ldiY += this.stepY;
        this.tMaxY += this.tDeltaY;

        this.trY += dt * this.normY;
        this.triY = getTrailingEdgeToVoxel(this.trY, this.stepY);

        this.axis = Axis.Y;
    }

    private void stepZ() {
        double dt = this.tMaxZ - this.distance;

        this.distance = this.tMaxZ;

        this.ldiZ += this.stepZ;
        this.tMaxZ += this.tDeltaZ;

        this.trZ += dt * this.normZ;
        this.triZ = getTrailingEdgeToVoxel(this.trZ, this.stepZ);

        this.axis = Axis.Z;
    }

    private void checkVoxels(Axis axis) {
        int x0 = (axis == Axis.X) ? this.ldiX : this.triX;
        int x1 = this.ldiX + this.stepX;

        int y0 = (axis == Axis.Y) ? this.ldiY : this.triY;
        int y1 = this.ldiY + this.stepY;

        int z0 = (axis == Axis.Z) ? this.ldiZ : this.triZ;
        int z1 = this.ldiZ + this.stepZ;

        for (int x = x0; x != x1; x += this.stepX) {
            for (int y = y0; y != y1; y += this.stepY) {
                for (int z = z0; z != z1; z += this.stepZ) {
                    CollisionTracer.IMPL.addTouchedBlock(x, y, z);

                    this.iterator.onBlockCollided(x, y, z);
                }
            }
        }
    }

    public interface SweepIterator {
        void onBlockCollided(int x, int y, int z);
    }

    private enum Axis {
        X, Y, Z
    }

    private static int getLeadingEdgeToVoxel(double coord, int step) {
        return MathHelper.floor(coord - (step * EPSILON));
    }

    private static int getTrailingEdgeToVoxel(double coord, int step) {
        return MathHelper.floor(coord + (step * EPSILON));
    }

    private static double getDistanceToNextVoxel(double val) {
        return Math.abs(1.0D / val);
    }

    private static double getNextVoxelCoordinate(double tDelta, double ld, int ldi, boolean dir) {
        if (tDelta < Double.POSITIVE_INFINITY) {
            double dist;

            if (dir) {
                dist = (ldi + 1) - ld;
            } else {
                dist = ld - ldi;
            }

            return tDelta * dist;
        } else {
            return Double.POSITIVE_INFINITY;
        }
    }
}
