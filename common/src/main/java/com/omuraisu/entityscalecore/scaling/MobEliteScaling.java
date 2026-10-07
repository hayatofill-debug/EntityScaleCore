package com.omuraisu.entityscalecore.scaling;

import com.omuraisu.entityscalecore.api.MobScaleResult;
import com.omuraisu.entityscalecore.config.ScalingConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class MobEliteScaling {

    private static final Set<Mob> ELITE_MOBS =
            Collections.newSetFromMap(
                    new WeakHashMap<>()
            );

    private static ScalingConfig config =
            new ScalingConfig();

    public static void setConfig(
            ScalingConfig config
    ) {
        MobEliteScaling.config = config;
    }

    public static void apply(
            Mob mob,
            MobScaleResult result
    ) {
        double multiplier = result.multiplier();

        double eliteChance = calculateEliteChance(
                multiplier
        );

        if (mob.getRandom().nextDouble() >= eliteChance) {
            return;
        }

        mob.setGlowingTag(true);

        ELITE_MOBS.add(mob);

        Component eliteName =
                Component.translatable(
                        "entity.entityscalecore.elite",
                        mob.getDisplayName()
                );

        if (config.eliteNameColorEnabled) {
            eliteName =
                    eliteName.copy().withStyle(
                            style ->
                                    style.withColor(
                                            config.eliteNameColor
                                    )
                    );
        }

        mob.setCustomName(
                eliteName
        );

        applyEliteEffects(
                mob,
                multiplier
        );
    }

    private static void applyEliteEffects(
            Mob mob,
            double multiplier
    ) {
        int effectCount = calculateEffectCount(
                mob,
                multiplier
        );
        int effectLevel = calculateEffectLevel(multiplier);

        // Strengthは必ず付与
        applyStrengthEffect(
                mob,
                effectLevel
        );

        int additionalEffects = effectCount - 1;

        if (additionalEffects <= 0) {
            return;
        }

        java.util.List<Integer> candidates =
                new java.util.ArrayList<>();

        // 0 = Speed
        // 1 = Resistance
        // 2 = Regeneration
        // 3 = Fire Resistance
        // 4 = Knockback Resistance

        candidates.add(0);
        candidates.add(1);
        candidates.add(2);
        candidates.add(3);
        candidates.add(4);

        int defenseCount = 0;

        int maxDefenseEffects;

        if (multiplier < 7.0) {
            maxDefenseEffects = 1;
        } else {
            maxDefenseEffects = 2;
        }

        while (
                additionalEffects > 0
                        && !candidates.isEmpty()
        ) {
            int index = mob.getRandom().nextInt(
                    candidates.size()
            );

            int candidate =
                    candidates.remove(index);

            boolean isDefenseEffect =
                    candidate == 1
                            || candidate == 3
                            || candidate == 4;

            if (
                    isDefenseEffect
                            && defenseCount >= maxDefenseEffects
            ) {
                continue;
            }

            switch (candidate) {

                case 0 -> applySpeedEffect(
                        mob,
                        effectLevel
                );

                case 1 -> applyResistanceEffect(
                        mob,
                        effectLevel
                );

                case 2 -> applyRegenerationEffect(
                        mob,
                        effectLevel
                );

                case 3 -> applyFireResistanceEffect(
                        mob
                );

                case 4 -> applyKnockbackResistance(
                        mob,
                        effectLevel
                );
            }

            if (isDefenseEffect) {
                defenseCount++;
            }

            additionalEffects--;

        }
    }

    private static int calculateEffectCount(
            Mob mob,
            double multiplier
    ) {
        if (multiplier < 2.0) {
            // Rank 1: 1
            return 1;

        } else if (multiplier < 3.0) {
            // Rank 2: 1
            return 1;

        } else if (multiplier < 4.0) {
            // Rank 3: 1～2
            return 1 + mob.getRandom().nextInt(2);

        } else if (multiplier < 5.0) {
            // Rank 4: 1～2
            return 1 + mob.getRandom().nextInt(2);

        } else if (multiplier < 6.0) {
            // Rank 5: 1～3
            return 1 + mob.getRandom().nextInt(3);

        } else if (multiplier < 7.0) {
            // Rank 6: 2～3
            return 2 + mob.getRandom().nextInt(2);

        } else if (multiplier < 8.0) {
            // Rank 7: 2～4
            return 2 + mob.getRandom().nextInt(3);

        } else if (multiplier < 9.0) {
            // Rank 8: 2～4
            return 2 + mob.getRandom().nextInt(3);

        } else if (multiplier < 10.0) {
            // Rank 9: 3～5
            return 3 + mob.getRandom().nextInt(3);

        } else {
            // Rank 10: 3～5
            return 3 + mob.getRandom().nextInt(3);
        }
    }

    private static int calculateEffectLevel(
            double multiplier
    ) {
        if (multiplier < 3.0) {
            return 0; // I
        } else if (multiplier < 5.0) {
            return 1; // II
        } else if (multiplier < 7.0) {
            return 2; // III
        } else if (multiplier < 9.0) {
            return 3; // IV
        } else {
            return 4; // V
        }
    }

    private static void applyStrengthEffect(
            Mob mob,
            int amplifier
    ) {
        mob.addEffect(
                new MobEffectInstance(
                        MobEffects.DAMAGE_BOOST,
                        -1,
                        amplifier,
                        false,
                        true,
                        true
                )
        );
    }

    private static void applySpeedEffect(
            Mob mob,
            int amplifier
    ) {
        mob.addEffect(
                new MobEffectInstance(
                        MobEffects.MOVEMENT_SPEED,
                        -1,
                        amplifier,
                        false,
                        true,
                        true
                )
        );
    }

    private static void applyResistanceEffect(
            Mob mob,
            int amplifier
    ) {
        int resistanceAmplifier =
                Math.min(amplifier, 3);

        mob.addEffect(
                new MobEffectInstance(
                        MobEffects.DAMAGE_RESISTANCE,
                        -1,
                        resistanceAmplifier,
                        false,
                        true,
                        true
                )
        );
    }

    private static void applyRegenerationEffect(
            Mob mob,
            int amplifier
    ) {
        mob.addEffect(
                new MobEffectInstance(
                        MobEffects.REGENERATION,
                        -1,
                        amplifier,
                        false,
                        true,
                        true
                )
        );
    }

    private static void applyFireResistanceEffect(
            Mob mob
    ) {
        mob.addEffect(
                new MobEffectInstance(
                        MobEffects.FIRE_RESISTANCE,
                        -1,
                        0,
                        false,
                        true,
                        true
                )
        );
    }

    private static double calculateEliteChance(
            double multiplier
    ) {
        double clampedMultiplier = Math.max(
                1.0,
                Math.min(10.0, multiplier)
        );

        return 0.01
                + (clampedMultiplier - 1.0)
                / 9.0
                * 0.02;
    }

    private static void applyKnockbackResistance(
            Mob mob,
            int amplifier
    ) {
        AttributeInstance attribute =
                mob.getAttribute(
                        Attributes.KNOCKBACK_RESISTANCE
                );

        if (attribute == null) {
            return;
        }

        double amount =
                0.10 * (amplifier + 1);

        attribute.addTransientModifier(
                new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                        "EntityScaleCore Elite Knockback Resistance",
                        amount,
                        net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION
                )
        );
    }

    public static void inheritElite(
            Mob parent,
            Mob child,
            double multiplier
    ) {
        if (!isElite(parent)) {
            return;
        }

        ELITE_MOBS.add(child);

        child.setGlowingTag(true);

        applyEliteEffects(
                child,
                multiplier
        );
    }

    public static boolean isElite(
            Mob mob
    ) {
        return ELITE_MOBS.contains(mob);
    }
}