package com.omuraisu.entityscalecore.core;

import com.omuraisu.entityscalecore.api.AreaHandle;
import com.omuraisu.entityscalecore.api.ScalingAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoreManager {

    private final ScalingAPI scalingAPI;

    private final Map<ResourceKey<Level>, Map<BlockPos, AreaHandle>> coreAreas = new HashMap<>();
    private final Map<ResourceKey<Level>, Map<BlockPos, CoreType>> coreTypes = new HashMap<>();

    public CoreManager(ScalingAPI scalingAPI) {
        this.scalingAPI = scalingAPI;
    }

    @SuppressWarnings("unused")
    public void registerCoreArea(
            ResourceKey<Level> dimension, BlockPos corePosition, String areaId,
            int minX, int maxX, int minZ, int maxZ, int minY, int maxY,
            double multiplier, int priority
    ) {
        AreaHandle handle = scalingAPI.registerArea(
                dimension, areaId, minX, maxX, minZ, maxZ, minY, maxY,
                multiplier, priority, List.of()
        );

        coreAreas.computeIfAbsent(dimension, key -> new HashMap<>()).put(corePosition, handle);
        coreTypes.computeIfAbsent(dimension, key -> new HashMap<>()).put(corePosition, CoreType.NORMAL);
    }

    @SuppressWarnings("unused")
    public void registerSafeCore(
            ResourceKey<Level> dimension, BlockPos corePosition, String areaId,
            int minX, int maxX, int minZ, int maxZ, int minY, int maxY, int priority
    ) {
        SafeCoreData data = new SafeCoreData(1.0, priority);

        AreaHandle handle = scalingAPI.registerArea(
                dimension, areaId, minX, maxX, minZ, maxZ, minY, maxY,
                data.multiplier(), data.priority(), List.of()
        );

        coreAreas.computeIfAbsent(dimension, key -> new HashMap<>()).put(corePosition, handle);
        coreTypes.computeIfAbsent(dimension, key -> new HashMap<>()).put(corePosition, CoreType.SAFE);
    }

    @SuppressWarnings("unused")
    public void registerRageCore(
            ResourceKey<Level> dimension, BlockPos corePosition, String areaId,
            int minX, int maxX, int minZ, int maxZ, int minY, int maxY,
            double multiplier, int priority
    ) {
        RageCoreData data = new RageCoreData(multiplier, priority);

        AreaHandle handle = scalingAPI.registerArea(
                dimension, areaId, minX, maxX, minZ, maxZ, minY, maxY,
                data.multiplier(), data.priority(), List.of()
        );

        coreAreas.computeIfAbsent(dimension, key -> new HashMap<>()).put(corePosition, handle);
        coreTypes.computeIfAbsent(dimension, key -> new HashMap<>()).put(corePosition, CoreType.RAGE);
    }

    @SuppressWarnings("unused")
    public boolean removeCoreArea(ResourceKey<Level> dimension, BlockPos corePosition) {
        Map<BlockPos, AreaHandle> dimensionAreas = coreAreas.get(dimension);

        if (dimensionAreas == null) {
            return false;
        }

        AreaHandle handle = dimensionAreas.remove(corePosition);
        Map<BlockPos, CoreType> dimensionTypes = coreTypes.get(dimension);

        if (dimensionTypes != null) {
            dimensionTypes.remove(corePosition);
            if (dimensionTypes.isEmpty()) {
                coreTypes.remove(dimension);
            }
        }

        if (handle == null) {
            return false;
        }

        handle.remove();

        if (dimensionAreas.isEmpty()) {
            coreAreas.remove(dimension);
        }

        return true;
    }

    @Nullable
    @SuppressWarnings("unused")
    public AreaHandle getArea(ResourceKey<Level> dimension, BlockPos corePosition) {
        Map<BlockPos, AreaHandle> dimensionAreas = coreAreas.get(dimension);
        return dimensionAreas != null ? dimensionAreas.get(corePosition) : null;
    }

    @SuppressWarnings("unused")
    public boolean hasCore(ResourceKey<Level> dimension, BlockPos corePosition) {
        Map<BlockPos, AreaHandle> dimensionAreas = coreAreas.get(dimension);
        return dimensionAreas != null && dimensionAreas.containsKey(corePosition);
    }

    @Nullable
    @SuppressWarnings("unused")
    public CoreType getCoreType(ResourceKey<Level> dimension, BlockPos corePosition) {
        Map<BlockPos, CoreType> dimensionTypes = coreTypes.get(dimension);
        return dimensionTypes != null ? dimensionTypes.get(corePosition) : null;
    }

    @Nullable
    public CoreType getCoreTypeByAreaId(ResourceKey<Level> dimension, String areaId) {
        Map<BlockPos, AreaHandle> dimensionAreas = coreAreas.get(dimension);
        if (dimensionAreas == null) {
            return null;
        }

        Map<BlockPos, CoreType> dimensionTypes = coreTypes.get(dimension);
        if (dimensionTypes == null) {
            return null;
        }

        for (Map.Entry<BlockPos, AreaHandle> entry : dimensionAreas.entrySet()) {
            if (entry.getValue().id().equals(areaId)) {
                return dimensionTypes.get(entry.getKey());
            }
        }

        return null;
    }
}