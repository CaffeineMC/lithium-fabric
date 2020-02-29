package me.jellysquid.mods.lithium.common.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelSet;
import net.minecraft.util.shape.VoxelShape;

/**
 * An efficient implementation of {@link VoxelShape} for a shape with no vertices.
 */
public class VoxelShapeEmpty extends VoxelShape {
    private static final DoubleList EMPTY_LIST = DoubleArrayList.wrap(new double[] { 0.0D });

    public VoxelShapeEmpty(VoxelSet voxels) {
        super(voxels);
    }

    @Override
    protected DoubleList getPointPositions(Direction.Axis axis) {
        return EMPTY_LIST;
    }

    @Override
    protected boolean contains(double x, double y, double z) {
        return false;
    }

    @Override
    public double getMinimum(Direction.Axis axis) {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public double getMaximum(Direction.Axis axis) {
        return Double.NEGATIVE_INFINITY;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }
}
