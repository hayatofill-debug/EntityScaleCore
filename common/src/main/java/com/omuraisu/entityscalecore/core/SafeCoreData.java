package com.omuraisu.entityscalecore.core;

public record SafeCoreData(double multiplier, int priority) {

    @SuppressWarnings("unused")
    public boolean isSuppressed() {
        return true;
    }
}