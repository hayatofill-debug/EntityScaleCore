package com.omuraisu.entityscalecore.api;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class MobScaleContext {

    private final double x;
    private final double y;
    private final double z;

    private final long gameTime;

    private final ResourceKey<Level> dimension;
    private final EntityType<?> entityType;

    private final double distanceFromOrigin;
    private final double weightedDistance;
    private final double depth;

    public MobScaleContext(
            double x,
            double y,
            double z,
            long gameTime,
            ResourceKey<Level> dimension,
            EntityType<?> entityType,
            double distanceFromOrigin,
            double weightedDistance,
            double depth
    ) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.gameTime = gameTime;
        this.dimension = dimension;
        this.entityType = entityType;
        this.distanceFromOrigin = distanceFromOrigin;
        this.weightedDistance = weightedDistance;
        this.depth = depth;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }

    public long gameTime() {
        return gameTime;
    }

    public ResourceKey<Level> dimension() {
        return dimension;
    }

    public EntityType<?> entityType() {
        return entityType;
    }

    public double distanceFromOrigin() {
        return distanceFromOrigin;
    }

    public double weightedDistance() {
        return weightedDistance;
    }

    public double depth() {
        return depth;
    }
}