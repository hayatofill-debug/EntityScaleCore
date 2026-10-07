package com.omuraisu.entityscalecore;

import com.omuraisu.entityscalecore.api.MobScaleContext;
import com.omuraisu.entityscalecore.api.MobScaleResult;
import com.omuraisu.entityscalecore.config.ScalingConfig;
import com.omuraisu.entityscalecore.config.ScalingConfigManager;
import com.omuraisu.entityscalecore.dimension.OriginManager;
import com.omuraisu.entityscalecore.fabric.FabricMobScalingPlatform;
import com.omuraisu.entityscalecore.scaling.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;

import java.nio.file.Path;

public class EntityScaleCore implements ModInitializer {

    private static EntityScaleCore instance;

    private MobScaler mobScaler;
    private ScalingAPIImpl scalingAPI;
    private ScalingConfig config;
    private OriginManager originManager;

    public EntityScaleCore() {
        instance = this;
    }

    public static EntityScaleCore getInstance() {
        return instance;
    }

    public ScalingConfig getConfig() {
        return config;
    }

    @Override
    public void onInitialize() {
        CommonClass.init();

        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("entityscalecore.json");
        config = ScalingConfigManager.load(configPath);

        MobEliteScaling.setConfig(config);

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            var overworld = server.overworld();

            originManager = OriginManager.get(server);

            scalingAPI = new ScalingAPIImpl(server, new ScalingCalculator());

            originManager.registerInitialOrigin(overworld, overworld.getSharedSpawnPos());
        });

        mobScaler = new MobScaler(new FabricMobScalingPlatform());
    }

    public void applyMobScaling(Mob mob, ServerLevel level) {
        double x = mob.getX();
        double y = mob.getY();
        double z = mob.getZ();

        var dimension = level.dimension();
        var origin = originManager.getOrigin(dimension);

        if (origin == null) {
            originManager.registerInitialOrigin(level, mob.blockPosition());
            origin = originManager.getOrigin(dimension);
        }

        if (origin == null) {
            return;
        }

        double dx = x - origin.getX();
        double dy = y - origin.getY();
        double dz = z - origin.getZ();

        double distanceFromOrigin = Math.sqrt(dx * dx + dy * dy + dz * dz);

        double weightedDistance = DistanceScaling.calculateWeightedDistance(
                dx, dy, dz, config.distanceYWeight
        );

        double depth = Math.max(0.0, origin.getY() - y);

        MobScaleContext context = new MobScaleContext(
                x, y, z, level.getGameTime(), dimension, mob.getType(),
                distanceFromOrigin, weightedDistance, depth
        );

        MobScaleResult result = scalingAPI.apply(context);

        mobScaler.apply(mob, result);
        MobLootScaling.registerMultiplier(mob, result.multiplier());
    }
}