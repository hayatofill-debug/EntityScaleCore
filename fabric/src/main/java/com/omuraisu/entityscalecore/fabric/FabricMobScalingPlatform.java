package com.omuraisu.entityscalecore.fabric;

import com.omuraisu.entityscalecore.api.MobScaleResult;
import com.omuraisu.entityscalecore.platform.MobScalingPlatform;
import com.omuraisu.entityscalecore.scaling.MobAttackScaling;
import com.omuraisu.entityscalecore.scaling.MobEquipmentScaling;
import com.omuraisu.entityscalecore.scaling.MobHealthScaling;
import com.omuraisu.entityscalecore.scaling.MobEliteScaling;
import net.minecraft.world.entity.Mob;

public class FabricMobScalingPlatform implements MobScalingPlatform {

    @Override
    public void apply(
            Mob mob,
            MobScaleResult result
    ) {
        MobHealthScaling.apply(
                mob,
                result
        );

        MobAttackScaling.apply(
                mob,
                result
        );

        MobEquipmentScaling.apply(
                mob,
                result
        );

        MobEliteScaling.apply(
                mob,
                result
        );
    }
}