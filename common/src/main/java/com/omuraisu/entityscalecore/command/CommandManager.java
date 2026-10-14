package com.omuraisu.entityscalecore.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.omuraisu.entityscalecore.api.*;
import com.omuraisu.entityscalecore.config.CoreJsonLoader;
import com.omuraisu.entityscalecore.config.ScalingConfig;
import com.omuraisu.entityscalecore.config.ScalingConfigManager;
import com.omuraisu.entityscalecore.core.CoreType;
import com.omuraisu.entityscalecore.scaling.DistanceScaling;
import com.omuraisu.entityscalecore.scaling.MobEliteScaling;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CommandManager {

    public static void register(
            CommandDispatcher<CommandSourceStack> dispatcher,
            Path configDir,
            Consumer<ScalingConfig> onConfigReload,
            Supplier<ScalingConfig> configSupplier
    ) {
        dispatcher.register(
                LiteralArgumentBuilder.<CommandSourceStack>literal("entityscalecore")
                        .requires(source -> true)
                        .then(
                                LiteralArgumentBuilder.<CommandSourceStack>literal("reload")
                                        .executes(context -> executeReload(context, configDir, onConfigReload))
                        )
                        .then(
                                LiteralArgumentBuilder.<CommandSourceStack>literal("info")
                                        .executes(context -> executeInfo(context, configSupplier))
                        )
        );
    }

    private static int executeReload(
            CommandContext<CommandSourceStack> context,
            Path configDir,
            Consumer<ScalingConfig> onConfigReload
    ) {
        CommandSourceStack source = context.getSource();

        try {
            Path configPath = configDir.resolve("scaling.json");
            ScalingConfig newConfig = ScalingConfigManager.load(configPath);

            onConfigReload.accept(newConfig);
            MobEliteScaling.setConfig(newConfig);

            ScalingAPI api = EntityScaleCoreAPI.get();
            if (api != null && api.getCoreManager() != null) {
                Path coreJsonPath = configDir.resolve("core_manager.json");
                boolean enableLog = newConfig.enableLoadingLog;
                CoreJsonLoader.loadAndRegister(coreJsonPath, api.getCoreManager(), enableLog);
            }

            source.sendSuccess(() -> Component.translatable("commands.entityscalecore.reload.success"), true);
            return 1;
        } catch (Exception e) {
            com.omuraisu.entityscalecore.Constants.LOG.error("Failed to reload configuration", e);
            source.sendFailure(Component.translatable("commands.entityscalecore.reload.failure"));
            return 0;
        }
    }

    private static int executeInfo(
            CommandContext<CommandSourceStack> context,
            Supplier<ScalingConfig> configSupplier
    ) {
        CommandSourceStack source = context.getSource();

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable("commands.entityscalecore.error.player_only"));
            return 0;
        }

        ScalingAPI api = EntityScaleCoreAPI.get();
        if (api == null) {
            source.sendFailure(Component.translatable("commands.entityscalecore.error.api_not_initialized"));
            return 0;
        }

        var level = player.serverLevel();
        var dimension = level.dimension();
        BlockPos playerPos = player.blockPosition();

        BlockPos origin = api.getOrigin(dimension);
        if (origin == null) {
            origin = level.getSharedSpawnPos();
        }

        double dx = playerPos.getX() - origin.getX();
        double dy = playerPos.getY() - origin.getY();
        double dz = playerPos.getZ() - origin.getZ();

        double distanceFromOrigin = Math.sqrt(dx * dx + dy * dy + dz * dz);

        ScalingConfig config = configSupplier.get();
        double yWeight = config != null ? config.distanceYWeight : 1.0;
        double weightedDistance = DistanceScaling.calculateWeightedDistance(dx, dy, dz, yWeight);
        double depth = Math.max(0.0, origin.getY() - playerPos.getY());

        MobScaleContext mobContext = new MobScaleContext(
                player.getX(), player.getY(), player.getZ(),
                level.getGameTime(), dimension, null,
                distanceFromOrigin, weightedDistance, depth
        );

        MobScaleResult result = api.apply(mobContext);

        source.sendSuccess(() -> Component.translatable("commands.entityscalecore.info.header"), false);
        source.sendSuccess(() -> Component.translatable("commands.entityscalecore.info.dimension", dimension.location().toString()), false);
        source.sendSuccess(() -> Component.translatable("commands.entityscalecore.info.distance", String.format("%.2f", distanceFromOrigin), String.format("%.2f", weightedDistance), String.format("%.2f", depth)), false);

        List<AreaHandle> areas = api.getAreas(dimension);
        AreaHandle matchedArea = null;
        for (AreaHandle area : areas) {
            if (area.contains((int) player.getX(), (int) player.getY(), (int) player.getZ())) {
                matchedArea = area;
                break;
            }
        }

        if (matchedArea != null) {
            String areaId = matchedArea.id();
            CoreType coreType = api.getCoreManager().getCoreTypeByAreaId(dimension, areaId);
            String typeName = coreType != null ? coreType.name() : "UNKNOWN";
            source.sendSuccess(() -> Component.translatable("commands.entityscalecore.info.area", areaId, typeName), false);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.entityscalecore.info.area_none"), false);
        }

        source.sendSuccess(() -> Component.translatable("commands.entityscalecore.info.multiplier", result.multiplier()), false);

        return 1;
    }
}