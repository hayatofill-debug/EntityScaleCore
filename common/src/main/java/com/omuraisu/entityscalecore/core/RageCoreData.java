package com.omuraisu.entityscalecore.core;

public record RageCoreData(double multiplier, int priority) {

    @SuppressWarnings("unused")
    public boolean isForced() {
        return true;
    }
}