package com.omuraisu.entityscalecore.mixin;

import com.omuraisu.entityscalecore.EntityScaleCore;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public class MobMixin {

    @Inject(
            method = "finalizeSpawn",
            at = @At("HEAD")
    )
    private void entityscalecore$onFinalizeSpawn(
            net.minecraft.world.level.ServerLevelAccessor level,
            net.minecraft.world.DifficultyInstance difficulty,
            net.minecraft.world.entity.MobSpawnType reason,
            net.minecraft.world.entity.SpawnGroupData spawnData,
            net.minecraft.nbt.CompoundTag dataTag,
            CallbackInfoReturnable<net.minecraft.world.entity.SpawnGroupData> cir
    ) {

        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        EntityScaleCore.getInstance().applyMobScaling(
                (Mob) (Object) this,
                serverLevel
        );
    }
}