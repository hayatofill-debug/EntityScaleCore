package com.omuraisu.entityscalecore.scaling;

public class DistanceScaling {

    @SuppressWarnings("unused")
    public static double calculateBonus(
            double dx,
            double dy,
            double dz,
            double startDistance,
            double blocksPerLevel,
            double bonusPerLevel,
            double maxBonus,
            double yWeight
    ) {
        double weightedDistance = calculateWeightedDistance(dx, dy, dz, yWeight);

        double distanceAfterStart = Math.max(0.0, weightedDistance - startDistance);
        double level = Math.floor(distanceAfterStart / blocksPerLevel);
        double bonus = level * bonusPerLevel;

        return Math.min(bonus, maxBonus);
    }

    public static double calculateWeightedDistance(
            double dx,
            double dy,
            double dz,
            double yWeight
    ) {
        return Math.sqrt(dx * dx + dy * dy * yWeight + dz * dz);
    }
}