package com.omuraisu.entityscalecore.platform;

import com.omuraisu.entityscalecore.api.MobScaleResult;
import net.minecraft.world.entity.Mob;

public interface MobScalingPlatform {

    void apply(
            Mob mob,
            MobScaleResult result
    );
}