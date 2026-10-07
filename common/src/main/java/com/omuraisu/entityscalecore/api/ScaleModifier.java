package com.omuraisu.entityscalecore.api;

@FunctionalInterface
public interface ScaleModifier {

    double modify(
            MobScaleContext context,
            double currentMultiplier
    );
}