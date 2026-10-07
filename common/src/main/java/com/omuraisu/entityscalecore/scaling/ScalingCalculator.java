package com.omuraisu.entityscalecore.scaling;

import com.omuraisu.entityscalecore.api.MobScaleContext;
import com.omuraisu.entityscalecore.config.ScalingConfig;

public class ScalingCalculator {

    public double calculateBaseMultiplier(
            MobScaleContext context,
            ScalingConfig config
    ) {
        double distanceBonus = 0.0;

        if (config.distanceEnabled) {
            double distanceAfterStart = Math.max(
                    0.0,
                    context.weightedDistance() - config.distanceStart
            );

            double distanceLevel = Math.floor(
                    distanceAfterStart / config.distanceBlocksPerLevel
            );

            distanceBonus = Math.min(
                    distanceLevel * config.distanceBonusPerLevel,
                    config.distanceMaxBonus
            );
        }

        long days =
                context.gameTime() / 24000L;

        double dayBonus =
                DayScaling.calculateBonus(
                        days,
                        config.dayBonusPerDay
                );

        if (config.dayMaxEnabled) {
            dayBonus = Math.min(
                    dayBonus,
                    config.dayMaxBonus
            );
        }

        double depthBonus = 0.0;

        if (config.depthEnabled) {
            depthBonus = Math.min(
                    context.depth() * config.depthBonusPerBlock,
                    config.depthMaxBonus
            );
        }

        return 1.0
                + distanceBonus
                + dayBonus
                + depthBonus;
    }
}