package com.omuraisu.entityscalecore.scaling;

import com.omuraisu.entityscalecore.api.MobScaleResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class MobAttackScaling {

    private static final UUID ATTACK_DAMAGE_MODIFIER_ID =
            UUID.fromString("b3c2d1e0-9f8a-4765-b4c3-2d1e0f9a8b7c");

    public static void apply(
            Mob mob,
            MobScaleResult result
    ) {
        double multiplier = result.multiplier();

        var attribute = mob.getAttribute(
                Attributes.ATTACK_DAMAGE
        );

        if (attribute == null) {
            return;
        }

        attribute.removeModifier(
                ATTACK_DAMAGE_MODIFIER_ID
        );

        double baseValue = attribute.getBaseValue();

        AttributeModifier modifier = new AttributeModifier(
                ATTACK_DAMAGE_MODIFIER_ID,
                "EntityScaleCore",
                baseValue * (multiplier - 1.0),
                AttributeModifier.Operation.ADDITION
        );

        attribute.addPermanentModifier(
                modifier
        );
    }
}