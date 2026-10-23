package com.omuraisu.entityscalecore;

import com.omuraisu.entityscalecore.api.MobScaleContext;
import com.omuraisu.entityscalecore.api.MobScaleResult;
import com.omuraisu.entityscalecore.command.CommandManager;
import com.omuraisu.entityscalecore.config.ScalingConfig;
import com.omuraisu.entityscalecore.config.ScalingConfigManager;
import com.omuraisu.entityscalecore.dimension.OriginManager;
import com.omuraisu.entityscalecore.forge.EntityScaleCoreConfigScreen;
import com.omuraisu.entityscalecore.forge.ForgeMobScalingPlatform;
import com.omuraisu.entityscalecore.scaling.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

@Mod(Constants.MOD_ID)
public class EntityScaleCore {

    private static EntityScaleCore instance;

    private final MobScaler mobScaler;
    private final ScalingConfig config;
    private ScalingAPIImpl scalingAPI;
    private OriginManager originManager;

    public EntityScaleCore() {
        instance = this;

        CommonClass.init();

        Path modConfigDir = FMLPaths.CONFIGDIR.get().resolve("entityscale");

        Path configPath = modConfigDir.resolve("entityscalecore.json");
        config = ScalingConfigManager.load(configPath);

        MobEliteScaling.setConfig(config);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
        MinecraftForge.EVENT_BUS.register(this);

        mobScaler = new MobScaler(new ForgeMobScalingPlatform());
    }

    public static EntityScaleCore getInstance() {
        return instance;
    }

    public ScalingConfig getConfig() {
        return config;
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (mc, parent) -> EntityScaleCoreConfigScreen.create(parent)
                )
        );
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        var server = event.getServer();
        ServerLevel overworld = server.overworld();

        originManager = OriginManager.get(server);

        scalingAPI = new ScalingAPIImpl(server, new ScalingCalculator());

        com.omuraisu.entityscalecore.api.EntityScaleCoreAPI.setInstance(scalingAPI);

        Path modConfigDir = FMLPaths.CONFIGDIR.get().resolve("entityscale");
        Path coreJsonPath = modConfigDir.resolve("core_manager.json");
        com.omuraisu.entityscalecore.config.CoreJsonLoader.loadAndRegister(
                coreJsonPath,
                scalingAPI.getCoreManager(),
                config.enableLoadingLog
        );

        originManager.registerInitialOrigin(
                overworld,
                overworld.getSharedSpawnPos()
        );
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        Path modConfigDir = FMLPaths.CONFIGDIR.get().resolve("entityscale");
        CommandManager.register(
                event.getDispatcher(),
                modConfigDir,
                MobEliteScaling::setConfig,
                () -> EntityScaleCore.getInstance().getConfig()
        );
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