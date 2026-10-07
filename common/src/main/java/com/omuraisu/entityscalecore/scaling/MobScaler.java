package com.omuraisu.entityscalecore.scaling;

import com.omuraisu.entityscalecore.api.MobScaleResult;
import com.omuraisu.entityscalecore.platform.MobScalingPlatform;
import net.minecraft.world.entity.Mob;

public class MobScaler {

    private final MobScalingPlatform platform;

    public MobScaler(
            MobScalingPlatform platform
    ) {
        this.platform = platform;
    }

    public void apply(
            Mob mob,
            MobScaleResult result
    ) {
        if (result.isSuppressed()) {
            return;
        }

        if (result.multiplier() <= 1.0) {
            return;
        }

        platform.apply(
                mob,
                result
        );
    }
}