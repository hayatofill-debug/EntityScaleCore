package com.omuraisu.entityscalecore.area;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AreaManager {

    private final Map<ResourceKey<Level>, List<Area>> areas = new HashMap<>();

    public void registerArea(ResourceKey<Level> dimension, Area area) {
        areas.computeIfAbsent(dimension, key -> new ArrayList<>()).add(area);
    }

    public boolean removeArea(ResourceKey<Level> dimension, String id) {
        List<Area> dimensionAreas = areas.get(dimension);

        if (dimensionAreas == null) {
            return false;
        }

        boolean removed = dimensionAreas.removeIf(area -> area.id().equals(id));

        if (dimensionAreas.isEmpty()) {
            areas.remove(dimension);
        }

        return removed;
    }

    public List<Area> getAreas(ResourceKey<Level> dimension) {
        List<Area> dimensionAreas = areas.get(dimension);

        if (dimensionAreas == null) {
            return List.of();
        }

        return List.copyOf(dimensionAreas);
    }

    @Nullable
    @SuppressWarnings("unused")
    public Area getArea(ResourceKey<Level> dimension, String id) {
        List<Area> dimensionAreas = areas.get(dimension);

        if (dimensionAreas == null) {
            return null;
        }

        return dimensionAreas.stream()
                .filter(area -> area.id().equals(id))
                .findFirst()
                .orElse(null);
    }

    @SuppressWarnings("unused")
    public List<Area> getAreasAt(ResourceKey<Level> dimension, double x, double y, double z) {
        List<Area> dimensionAreas = areas.get(dimension);

        if (dimensionAreas == null) {
            return List.of();
        }

        return dimensionAreas.stream()
                .filter(area -> area.contains(x, y, z))
                .sorted(Comparator.comparingInt(Area::priority).reversed())
                .toList();
    }

    public List<Area> getAreasAt(
            ResourceKey<Level> dimension,
            double x, double y, double z,
            ResourceLocation entityType
    ) {
        List<Area> dimensionAreas = areas.get(dimension);

        if (dimensionAreas == null) {
            return List.of();
        }

        return dimensionAreas.stream()
                .filter(area -> area.contains(x, y, z))
                .filter(area -> area.targets(entityType))
                .sorted(Comparator.comparingInt(Area::priority).reversed())
                .toList();
    }

    @Nullable
    public Area getHighestPriorityArea(
            ResourceKey<Level> dimension,
            double x, double y, double z,
            ResourceLocation entityType
    ) {
        List<Area> matchingAreas = getAreasAt(dimension, x, y, z, entityType);

        if (matchingAreas.isEmpty()) {
            return null;
        }

        return matchingAreas.get(0);
    }
}