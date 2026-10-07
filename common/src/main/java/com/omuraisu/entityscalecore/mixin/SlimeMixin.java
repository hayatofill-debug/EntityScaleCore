package com.omuraisu.entityscalecore.mixin;

import com.omuraisu.entityscalecore.api.MobScaleResult;
import com.omuraisu.entityscalecore.api.ScaleSource;
import com.omuraisu.entityscalecore.scaling.MobAttackScaling;
import com.omuraisu.entityscalecore.scaling.MobEliteScaling;
import com.omuraisu.entityscalecore.scaling.MobHealthScaling;
import com.omuraisu.entityscalecore.scaling.MobLootScaling;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Slime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Slime.class)
public class SlimeMixin {

    @ModifyArg(
            method = "remove",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"
            ),
            index = 0
    )
    private Entity entityscalecore$inheritMultiplier(
            Entity entity
    ) {
        Slime parent =
                (Slime) (Object) this;

        if (!(entity instanceof Slime child)) {
            return entity;
        }

        double parentMultiplier =
                MobLootScaling.getMultiplier(parent);

        if (parentMultiplier > 1.0) {
            MobLootScaling.registerMultiplier(
                    child,
                    parentMultiplier
            );

            MobScaleResult result =
                    new MobScaleResult(
                            parentMultiplier,
                            false,
                            true,
                            ScaleSource.OTHER
                    );

            MobHealthScaling.apply(
                    child,
                    result
            );

            MobAttackScaling.apply(
                    child,
                    result
            );

            MobEliteScaling.inheritElite(
                    parent,
                    child,
                    parentMultiplier
            );
        }

        return entity;
    }
}