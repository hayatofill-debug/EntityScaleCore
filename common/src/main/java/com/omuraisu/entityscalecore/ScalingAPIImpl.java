package com.omuraisu.entityscalecore;

import com.omuraisu.entityscalecore.api.AreaHandle;
import com.omuraisu.entityscalecore.api.MobScaleContext;
import com.omuraisu.entityscalecore.api.MobScaleResult;
import com.omuraisu.entityscalecore.api.ScaleModifier;
import com.omuraisu.entityscalecore.api.ScalingAPI;
import com.omuraisu.entityscalecore.area.Area;
import com.omuraisu.entityscalecore.area.AreaHandleImpl;
import com.omuraisu.entityscalecore.area.AreaManager;
import com.omuraisu.entityscalecore.config.ScalingConfig;
import com.omuraisu.entityscalecore.core.CoreManager;
import com.omuraisu.entityscalecore.core.CoreType;
import com.omuraisu.entityscalecore.dimension.OriginManager;
import com.omuraisu.entityscalecore.scaling.ScalingCalculator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScalingAPIImpl implements ScalingAPI {

    private final MinecraftServer server;

    private final AreaManager areaManager = new AreaManager();

    private final CoreManager coreManager;

    private final List<ScaleModifier> modifiers = new ArrayList<>();

    private final ScalingConfig config = new ScalingConfig();

    private final ScalingCalculator scalingCalculator;

    public ScalingAPIImpl(
            MinecraftServer server,
            ScalingCalculator scalingCalculator
    ) {
        this.server = server;
        this.scalingCalculator = scalingCalculator;
        this.coreManager = new CoreManager(this);
    }

    @Override
    public double getScale(
            MobScaleContext context
    ) {
        double multiplier = scalingCalculator.calculateBaseMultiplier(
                context,
                config
        );

        ResourceLocation entityTypeId =
                BuiltInRegistries.ENTITY_TYPE.getKey(
                        context.entityType()
                );

        Area area = areaManager.getHighestPriorityArea(
                context.dimension(),
                context.x(),
                context.y(),
                context.z(),
                entityTypeId
        );

        if (area != null) {
            multiplier *= area.multiplier();
        }

        for (ScaleModifier modifier : modifiers) {
            multiplier = modifier.modify(
                    context,
                    multiplier
            );
        }

        return multiplier;
    }

    private Area getApplicableArea(
            MobScaleContext context
    ) {
        ResourceLocation entityTypeId =
                BuiltInRegistries.ENTITY_TYPE.getKey(
                        context.entityType()
                );

        return areaManager.getHighestPriorityArea(
                context.dimension(),
                context.x(),
                context.y(),
                context.z(),
                entityTypeId
        );
    }

    @Override
    public void registerModifier(
            ScaleModifier modifier
    ) {
        modifiers.add(modifier);
    }

    @Override
    public AreaHandle registerArea(
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
    ) {
        Area area = new Area(
                id,
                minX,
                maxX,
                minZ,
                maxZ,
                minY,
                maxY,
                multiplier,
                priority,
                targetEntities
        );

        areaManager.registerArea(
                dimension,
                area
        );

        return new AreaHandleImpl(
                areaManager,
                dimension,
                id
        );
    }

    @Override
    public boolean removeArea(
            ResourceKey<Level> dimension,
            String id
    ) {
        return areaManager.removeArea(
                dimension,
                id
        );
    }

    @Override
    public List<AreaHandle> getAreas(
            ResourceKey<Level> dimension
    ) {
        return areaManager.getAreas(dimension)
                .stream()
                .map(area -> new AreaHandleImpl(
                        areaManager,
                        dimension,
                        area.id()
                ))
                .map(handle -> (AreaHandle) handle)
                .toList();
    }

    @Override
    public MobScaleResult apply(MobScaleContext context) {

        double multiplier = getScale(context);

        Area area = getApplicableArea(context);

        if (area == null) {
            return new MobScaleResult(
                    multiplier,
                    false,
                    false,
                    com.omuraisu.entityscalecore.api.ScaleSource.OTHER
            );
        }

        CoreType coreType = coreManager.getCoreTypeByAreaId(
                context.dimension(),
                area.id()
        );

        if (coreType == CoreType.SAFE) {
            return new MobScaleResult(
                    1.0,
                    true,
                    false,
                    com.omuraisu.entityscalecore.api.ScaleSource.SAFE_CORE
            );
        }

        if (coreType == CoreType.RAGE) {
            return new MobScaleResult(
                    multiplier,
                    false,
                    true,
                    com.omuraisu.entityscalecore.api.ScaleSource.RAGE_CORE
            );
        }

        return new MobScaleResult(
                multiplier,
                false,
                false,
                com.omuraisu.entityscalecore.api.ScaleSource.AREA
        );
    }

    @Override
    @Nullable
    public BlockPos getOrigin(ResourceKey<Level> dimension) {
        return OriginManager.get(this.server).getOrigin(dimension);
    }

    @Override
    public Map<ResourceKey<Level>, BlockPos> getOrigins() {
        return OriginManager.get(this.server).getOrigins();
    }
}