package com.omuraisu.entityscalecore.scaling;

import net.minecraft.world.entity.Mob;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class MobLootScaling {

    private static final Map<Mob, Double> MOB_MULTIPLIERS =
            Collections.synchronizedMap(
                    new WeakHashMap<>()
            );

    public static void registerMultiplier(
            Mob mob,
            double multiplier
    ) {
        if (mob == null) {
            return;
        }

        if (multiplier <= 1.0) {
            return;
        }

        MOB_MULTIPLIERS.put(
                mob,
                multiplier
        );
    }

    public static double getMultiplier(
            Mob mob
    ) {
        if (mob == null) {
            return 1.0;
        }

        return MOB_MULTIPLIERS.getOrDefault(
                mob,
                1.0
        );
    }

    public static int getLootRollCount(
            Mob mob
    ) {
        double multiplier = getMultiplier(mob);

        int rollCount = Math.max(
                1,
                (int) Math.floor(multiplier)
        );

        if (MobEliteScaling.isElite(mob)) {
            rollCount++;
        }

        return rollCount;
    }

}