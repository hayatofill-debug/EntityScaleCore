package com.omuraisu.entityscalecore.api;

public class EntityScaleCoreAPI {

    private static ScalingAPI instance;

    public static void setInstance(ScalingAPI api) {
        instance = api;
    }

    public static void reset() {
        instance = null;
    }

    public static ScalingAPI get() {
        if (instance == null) {
            throw new IllegalStateException("EntityScaleCore API is not ready yet! Ensure the world is loaded.");
        }
        return instance;
    }
}