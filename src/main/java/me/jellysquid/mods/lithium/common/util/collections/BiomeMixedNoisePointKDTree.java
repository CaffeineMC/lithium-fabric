package me.jellysquid.mods.lithium.common.util.collections;

import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


/**
 * A kd-tree for the 5D {@link Biome.MixedNoisePoint} class.
 */
public interface BiomeMixedNoisePointKDTree {
    int K = 5;
    Axis[] AXIS_VALUES = Axis.values();

    static BiomeMixedNoisePointKDTree build(List<Biome.MixedNoisePoint> biomePoints) {
        return build(biomePoints, 0);
    }

    private static Node build(List<Biome.MixedNoisePoint> biomePoints, int depth) {
        if (biomePoints.isEmpty()) {
            return null;
        }

        Axis axis = AXIS_VALUES[depth % K];

        biomePoints.sort(axis);

        int median = biomePoints.size() / 2;
        Biome.MixedNoisePoint medianValue = biomePoints.get(median);
        List<Biome.MixedNoisePoint> left = new ArrayList<>(biomePoints.subList(0, median));
        List<Biome.MixedNoisePoint> right = new ArrayList<>(biomePoints.subList(median + 1, biomePoints.size()));

        return new Node(axis, medianValue, build(left, depth + 1), build(right, depth + 1));
    }

    Biome.MixedNoisePoint nearestBiomeTo(Biome.MixedNoisePoint point);

    enum Axis implements Comparator<Biome.MixedNoisePoint> {
        TEMPERATURE {
            @Override
            public float apply(Biome.MixedNoisePoint value) {
                return value.temperature;
            }
        },
        HUMIDITY {
            @Override
            public float apply(Biome.MixedNoisePoint value) {
                return value.humidity;
            }
        },
        ALTITUDE {
            @Override
            public float apply(Biome.MixedNoisePoint value) {
                return value.altitude;
            }
        },
        WEIRDNESS {
            @Override
            public float apply(Biome.MixedNoisePoint value) {
                return value.weirdness;
            }
        },
        WEIGHT {
            @Override
            public float apply(Biome.MixedNoisePoint value) {
                return value.weight;
            }
        };

        abstract float apply(Biome.MixedNoisePoint value);

        public float distance(Biome.MixedNoisePoint p1, Biome.MixedNoisePoint p2) {
            return apply(p1) - apply(p2);
        }

        public float distance2(Biome.MixedNoisePoint p1, Biome.MixedNoisePoint p2) {
            float dist = distance(p1, p2);
            return dist * dist;
        }

        @Override
        public int compare(Biome.MixedNoisePoint o1, Biome.MixedNoisePoint o2) {
            return Float.compare(apply(o1), apply(o2));
        }
    }

    record Node(Axis axis,
                Biome.MixedNoisePoint location,
                @Nullable BiomeMixedNoisePointKDTree.Node leftChild,
                @Nullable BiomeMixedNoisePointKDTree.Node rightChild) implements BiomeMixedNoisePointKDTree {

        private static Biome.MixedNoisePoint nearestBiomeTo(final Biome.MixedNoisePoint point, @Nullable final Node tree, Biome.MixedNoisePoint best, float bestDist) {
            if (tree == null) {
                return best;
            }

            // Test the exact middle of the split
            float dist = point.calculateDistanceTo(tree.location);
            if (dist < bestDist) {
                best = tree.location;
                bestDist = dist;
            }

            // Order the child nodes by likelihood of success
            Node firstChild, secondChild;
            if (tree.axis.compare(point, tree.location) < 0) {
                // Left branch
                firstChild = tree.leftChild;
                secondChild = tree.rightChild;
            } else {
                // Right branch
                firstChild = tree.rightChild;
                secondChild = tree.leftChild;
            }

            // Update best with info from the "likely" branch
            best = nearestBiomeTo(point, firstChild, best, bestDist);
            bestDist = point.calculateDistanceTo(best);

            // Intersect a hypersphere of radius bestDist with the hyperplane defined by [tree] to see if we need to check the other side of the hyperplane
            if (firstChild != null && bestDist < tree.axis.distance2(point, tree.location)) {
                // We've already found a closer point than the closest point on the hyperplane; skip checking the other side
                return best;
            } else {
                // Check the other side
                return nearestBiomeTo(point, secondChild, best, bestDist);
            }
        }

        @Override
        public Biome.MixedNoisePoint nearestBiomeTo(Biome.MixedNoisePoint point) {
            return nearestBiomeTo(point, this, this.location, this.location.calculateDistanceTo(point));
        }
    }
}
