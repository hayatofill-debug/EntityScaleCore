package com.omuraisu.entityscalecore.api;

public record ScalingBreakdown(
        double distanceBonus,
        double dayBonus,
        double depthBonus,
        double areaMultiplier,
        double totalMultiplier
) {}