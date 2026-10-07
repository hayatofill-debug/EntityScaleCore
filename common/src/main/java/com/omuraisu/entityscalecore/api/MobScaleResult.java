package com.omuraisu.entityscalecore.api;

public class MobScaleResult {

    private final double multiplier;
    private final boolean isSuppressed;
    private final boolean isForced;
    private final ScaleSource source;

    public MobScaleResult(
            double multiplier,
            boolean isSuppressed,
            boolean isForced,
            ScaleSource source
    ) {
        this.multiplier = multiplier;
        this.isSuppressed = isSuppressed;
        this.isForced = isForced;
        this.source = source;
    }

    public double multiplier() {
        return multiplier;
    }

    public boolean isSuppressed() {
        return isSuppressed;
    }

    public boolean isForced() {
        return isForced;
    }

    public ScaleSource source() {
        return source;
    }
}