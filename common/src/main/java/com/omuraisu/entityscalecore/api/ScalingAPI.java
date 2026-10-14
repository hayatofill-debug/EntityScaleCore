package com.omuraisu.entityscalecore.api;

import com.omuraisu.entityscalecore.core.CoreManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public interface ScalingAPI {

    double getScale(MobScaleContext context);

    void registerModifier(ScaleModifier modifier);

    AreaHandle registerArea(
            ResourceKey<Level> dimension,
            String id,
            int minX,
            int maxX,
            int minZ,
            int maxZ,
            int minY,
            int maxY,
            double multiplier,
            int priority,
            List<ResourceLocation> targetEntities
    );

    boolean removeArea(ResourceKey<Level> dimension, String id);

    List<AreaHandle> getAreas(ResourceKey<Level> dimension);

    MobScaleResult apply(MobScaleContext context);

    @Nullable
    BlockPos getOrigin(ResourceKey<Level> dimension);

    Map<ResourceKey<Level>, BlockPos> getOrigins();

    ScalingBreakdown getBreakdown(MobScaleContext context);

    CoreManager getCoreManager();
}