package com.omuraisu.entityscalecore.core;

import com.google.gson.annotations.SerializedName;

public enum CoreType {
    @SerializedName("normal")
    NORMAL(false, false),

    @SerializedName("safe")
    SAFE(true, false),

    @SerializedName("rage")
    RAGE(false, true),

    @SerializedName("custom")
    CUSTOM(false, false),

    @SerializedName("custom_safe")
    CUSTOM_SAFE(true, false),

    @SerializedName("custom_rage")
    CUSTOM_RAGE(false, true);

    private final boolean suppressed;
    private final boolean forced;

    CoreType(boolean suppressed, boolean forced) {
        this.suppressed = suppressed;
        this.forced = forced;
    }

    public boolean isSuppressed() {
        return suppressed;
    }

    public boolean isForced() {
        return forced;
    }

    public static CoreType fromString(String name) {
        if (name == null) return NORMAL;
        try {
            return CoreType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return NORMAL;
        }
    }
}