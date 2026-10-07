package com.omuraisu.entityscalecore.api;

@FunctionalInterface
public interface AreaModifier {

    double modify(
            MobScaleContext context,
            double currentMultiplier
    );
}