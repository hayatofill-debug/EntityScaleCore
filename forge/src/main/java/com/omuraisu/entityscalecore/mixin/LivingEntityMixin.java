package com.omuraisu.entityscalecore.mixin;

import com.omuraisu.entityscalecore.scaling.MobLootScaling;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Consumer;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Redirect(
            method = "dropFromLootTable",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;JLjava/util/function/Consumer;)V"
            )
    )
    private void entityscalecore$scaleLootRolls(
            LootTable lootTable,
            LootParams params,
            long seed,
            Consumer<net.minecraft.world.item.ItemStack> output
    ) {
        LivingEntity entity =
                (LivingEntity) (Object) this;

        if (!(entity instanceof Mob mob)) {
            lootTable.getRandomItems(
                    params,
                    seed,
                    output
            );
            return;
        }

        int rollCount =
                MobLootScaling.getLootRollCount(
                        mob
                );

        for (int i = 0; i < rollCount; i++) {
            lootTable.getRandomItems(
                    params,
                    seed + i,
                    output
            );
        }
    }
}