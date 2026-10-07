package com.omuraisu.entityscalecore.scaling;

import com.omuraisu.entityscalecore.api.MobScaleResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class MobHealthScaling {

    private static final UUID MAX_HEALTH_MODIFIER_ID =
            UUID.fromString("7f5b7c4a-2e1d-4f8a-9c31-6a2b8d5e1047");

    public static void apply(Mob mob, MobScaleResult result) {
        double multiplier = result.multiplier();

        var attribute = mob.getAttribute(Attributes.MAX_HEALTH);
        if (attribute == null) {
            return;
        }

        attribute.removeModifier(MAX_HEALTH_MODIFIER_ID);

        double baseValue = attribute.getBaseValue();

        AttributeModifier modifier = new AttributeModifier(
                MAX_HEALTH_MODIFIER_ID,
                "EntityScaleCore",
                baseValue * (multiplier - 1.0),
                AttributeModifier.Operation.ADDITION
        );

        attribute.addPermanentModifier(modifier);

        mob.setHealth(mob.getMaxHealth());
    }
}