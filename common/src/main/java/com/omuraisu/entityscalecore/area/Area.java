package com.omuraisu.entityscalecore.area;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record Area(
        String id,
        int minX,
        int maxX,
        int minZ,
        int maxZ,
        int minY,
        int maxY,
        double multiplier,
        int priority,
        List<ResourceLocation> targetEntities
) {
    public Area {
        int actualMinX = Math.min(minX, maxX);
        int actualMaxX = Math.max(minX, maxX);
        minX = actualMinX;
        maxX = actualMaxX;

        int actualMinZ = Math.min(minZ, maxZ);
        int actualMaxZ = Math.max(minZ, maxZ);
        minZ = actualMinZ;
        maxZ = actualMaxZ;

        int actualMinY = Math.min(minY, maxY);
        int actualMaxY = Math.max(minY, maxY);
        minY = actualMinY;
        maxY = actualMaxY;

        targetEntities = List.copyOf(targetEntities);
    }

    public boolean contains(double x, double y, double z) {
        return x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
    }

    public boolean targets(ResourceLocation entityType) {
        return targetEntities.isEmpty() || targetEntities.contains(entityType);
    }
}