package me.jellysquid.mods.lithium.common.shapes;

import it.unimi.dsi.fastutil.doubles.*;
import me.jellysquid.mods.lithium.mixin.minimal_nonvanilla.collisions.empty_space.ArrayVoxelShapeInvoker;
import me.jellysquid.mods.lithium.mixin.minimal_nonvanilla.collisions.empty_space.BitSetVoxelSetAccessor;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;

public class VoxelShapeHelper {

    /**
     * Get the closest point to the target point that is inside the collidingShape but not inside any of the boxes.
     * [VanillaCopy] This is a copy of VoxelShape.getClosestPointTo(Vec3d target) with the difference that it does
     * not construct many unnecessary shapes. Instead, the resulting shape is constructed directly from a bitset.
     * <p>
     * [NotVanilla] This method is slightly different from vanilla if there are multiple point positions from
     * {@link VoxelShape#getCoords(Direction.Axis)} that are within 1e-7 of each other. In that case, vanilla
     * discards one of the point positions, effectively shifting one shape boundary by up to 1e-7.
     * This implementation just keeps all point positions, possibly leading to a completely different result.
     *
     * @param target The target point
     * @param collidingShape The shape that the point must be inside
     * @param boxes The boxes that the point must not be inside
     * @return The closest point to the target point that is inside the collidingShape but not inside any of the boxes
     */
    public static Optional<Vec3> getClosestPointTo(Vec3 target, VoxelShape collidingShape, List<AABB> boxes) {
        // First create a shape that contains the volume: collidingShape \ boxes

        // Create the point positions for x, y and z:
        DoubleOpenHashSet xPoints = new DoubleOpenHashSet();
        DoubleOpenHashSet yPoints = new DoubleOpenHashSet();
        DoubleOpenHashSet zPoints = new DoubleOpenHashSet();

        xPoints.addAll(collidingShape.getCoords(Direction.Axis.X));
        yPoints.addAll(collidingShape.getCoords(Direction.Axis.Y));
        zPoints.addAll(collidingShape.getCoords(Direction.Axis.Z));

        double minX = collidingShape.min(Direction.Axis.X);
        double maxX = collidingShape.max(Direction.Axis.X);
        double minY = collidingShape.min(Direction.Axis.Y);
        double maxY = collidingShape.max(Direction.Axis.Y);
        double minZ = collidingShape.min(Direction.Axis.Z);
        double maxZ = collidingShape.max(Direction.Axis.Z);

        for (AABB box : boxes) {
            if (box.minX > minX) {
                xPoints.add(box.minX);
            }
            if (box.maxX < maxX) {
                xPoints.add(box.maxX);
            }
            if (box.minY > minY) {
                yPoints.add(box.minY);
            }
            if (box.maxY < maxY) {
                yPoints.add(box.maxY);
            }
            if (box.minZ > minZ) {
                zPoints.add(box.minZ);
            }
            if (box.maxZ < maxZ) {
                zPoints.add(box.maxZ);
            }
        }

        // Convert the point positions to lists:
        DoubleArrayList xList = new DoubleArrayList(xPoints);
        DoubleList yList = new DoubleArrayList(yPoints);
        DoubleList zList = new DoubleArrayList(zPoints);
        xList.sort(DoubleComparators.NATURAL_COMPARATOR);
        yList.sort(DoubleComparators.NATURAL_COMPARATOR);
        zList.sort(DoubleComparators.NATURAL_COMPARATOR);

        // Fast index lookup:
        Double2IntMap xIndex = new Double2IntOpenHashMap();
        Double2IntMap yIndex = new Double2IntOpenHashMap();
        Double2IntMap zIndex = new Double2IntOpenHashMap();

        for (int i = 0; i < xList.size(); i++) {
            xIndex.put(xList.getDouble(i), i);
        }
        for (int i = 0; i < yList.size(); i++) {
            yIndex.put(yList.getDouble(i), i);
        }
        for (int i = 0; i < zList.size(); i++) {
            zIndex.put(zList.getDouble(i), i);
        }

        //Size is 1 smaller because we have n points and n-1 spaces between them
        int xSize = xList.size() - 1;
        int ySize = yList.size() - 1;
        int zSize = zList.size() - 1;

        BitSetDiscreteVoxelShape bitSetVoxelSet = new BitSetDiscreteVoxelShape(xSize, ySize, zSize);
        //Initialize min/max of the voxelSet:
        bitSetVoxelSet.fill(0,0,0);
        bitSetVoxelSet.fill(xSize, ySize, zSize);
        //Clear the values have just written, but without updating min/max x, y, z values. The voxelSet is empty
        // but has the correct min/max values after this
        BitSet bitSet = ((BitSetVoxelSetAccessor) (Object) bitSetVoxelSet).getStorage();
        bitSet.clear();

        //Add all points inside the collidingShape, remove all points in other boxes afterward
        initVoxelSet(bitSet, collidingShape, boxes, xList, yList, zList, xIndex, yIndex, zIndex, xSize, ySize, zSize);

        //Get the closest point like vanilla, because the conflict resolution (two points with same distance)
        // details need to be the same.
        VoxelShape shape = ArrayVoxelShapeInvoker.init(bitSetVoxelSet, xList, yList, zList);
        return shape.closestPointTo(target);
    }

    private static void initVoxelSet(BitSet voxelSet, VoxelShape collidingShape, List<AABB> boxes, DoubleArrayList xList, DoubleList yList, DoubleList zList, Double2IntMap xIndex, Double2IntMap yIndex, Double2IntMap zIndex, int xSize, int ySize, int zSize) {
        //Add all points inside the collidingShape, remove all points in other boxes afterward
        for(AABB collidingBox : collidingShape.toAabbs()) {
            int minX = xIndex.get(collidingBox.minX);
            int maxX = xIndex.get(collidingBox.maxX);
            int minY = yIndex.get(collidingBox.minY);
            int maxY = yIndex.get(collidingBox.maxY);
            int minZ = zIndex.get(collidingBox.minZ);
            int maxZ = zIndex.get(collidingBox.maxZ);
            for (int x = minX; x < maxX; x++) {
                for (int y = minY; y < maxY; y++) {
                    for (int z = minZ; z < maxZ; z++) {
                        voxelSet.set(getIndex(x, y, z, xSize, ySize, zSize), true);
                    }
                }
            }
        }
        BitSet remove = new BitSet(voxelSet.size());
        for (AABB box : boxes) {
            int minX = xIndex.getOrDefault(box.minX, 0);
            int maxX = xIndex.getOrDefault(box.maxX, xSize);
            int minY = yIndex.getOrDefault(box.minY, 0);
            int maxY = yIndex.getOrDefault(box.maxY, ySize);
            int minZ = zIndex.getOrDefault(box.minZ, 0);
            int maxZ = zIndex.getOrDefault(box.maxZ, zSize);
            for (int x = minX; x < maxX; x++) {
                for (int y = minY; y < maxY; y++) {
                    for (int z = minZ; z < maxZ; z++) {
                        remove.set(getIndex(x, y, z, xSize, ySize, zSize));
                    }
                }
            }
        }
        voxelSet.andNot(remove);
    }

    private static int getIndex(int x, int y, int z, int sizeX, int sizeY, int sizeZ) {
        return (x * sizeY + y) * sizeZ + z;
    }
}
