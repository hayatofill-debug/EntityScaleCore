package com.omuraisu.entityscalecore.forge;

import com.omuraisu.entityscalecore.EntityScaleCore;
import com.omuraisu.entityscalecore.config.ScalingConfig;
import com.omuraisu.entityscalecore.config.ScalingConfigManager;
import com.omuraisu.entityscalecore.scaling.MobEliteScaling;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.loading.FMLPaths;

public class EntityScaleCoreConfigScreen {

    public static Screen create(Screen parent) {

        ScalingConfig config =
                EntityScaleCore.getInstance()
                        .getConfig();

        ConfigBuilder builder =
                ConfigBuilder.create()
                        .setParentScreen(parent)
                        .setTitle(
                                Component.translatable(
                                        "config.entityscalecore.title"
                                )
                        );

        ConfigEntryBuilder entryBuilder =
                builder.entryBuilder();

        // =========================
        // 距離設定
        // =========================

        ConfigCategory distance =
                builder.getOrCreateCategory(
                        Component.translatable(
                                "config.entityscalecore.category.distance"
                        )
                );

        distance.addEntry(
                entryBuilder.startBooleanToggle(
                                Component.translatable(
                                        "config.entityscalecore.distance.enabled"
                                ),
                                config.distanceEnabled
                        )
                        .setDefaultValue(true)
                        .setSaveConsumer(
                                value -> config.distanceEnabled = value
                        )
                        .build()
        );

        distance.addEntry(
                entryBuilder.startDoubleField(
                                Component.translatable(
                                        "config.entityscalecore.distance.start"
                                ),
                                config.distanceStart
                        )
                        .setDefaultValue(1000.0)
                        .setMin(0.0)
                        .setMax(1_000_000_000.0)
                        .setSaveConsumer(
                                value -> config.distanceStart = value
                        )
                        .build()
        );

        distance.addEntry(
                entryBuilder.startDoubleField(
                                Component.translatable(
                                        "config.entityscalecore.distance.blocks_per_level"
                                ),
                                config.distanceBlocksPerLevel
                        )
                        .setDefaultValue(100.0)
                        .setMin(0.0001)
                        .setMax(1_000_000_000.0)
                        .setSaveConsumer(
                                value -> config.distanceBlocksPerLevel = value
                        )
                        .build()
        );

        distance.addEntry(
                entryBuilder.startDoubleField(
                                Component.translatable(
                                        "config.entityscalecore.distance.bonus_per_level"
                                ),
                                config.distanceBonusPerLevel
                        )
                        .setDefaultValue(0.1)
                        .setMin(0.0)
                        .setMax(100.0)
                        .setSaveConsumer(
                                value -> config.distanceBonusPerLevel = value
                        )
                        .build()
        );

        distance.addEntry(
                entryBuilder.startDoubleField(
                                Component.translatable(
                                        "config.entityscalecore.distance.max_bonus"
                                ),
                                config.distanceMaxBonus
                        )
                        .setDefaultValue(10.0)
                        .setMin(0.0)
                        .setMax(100.0)
                        .setSaveConsumer(
                                value -> config.distanceMaxBonus = value
                        )
                        .build()
        );

        distance.addEntry(
                entryBuilder.startDoubleField(
                                Component.translatable(
                                        "config.entityscalecore.distance.y_weight"
                                ),
                                config.distanceYWeight
                        )
                        .setDefaultValue(0.25)
                        .setMin(0.0)
                        .setMax(100.0)
                        .setSaveConsumer(
                                value -> config.distanceYWeight = value
                        )
                        .build()
        );

        // =========================
        // 深さ設定
        // =========================

        ConfigCategory depth =
                builder.getOrCreateCategory(
                        Component.translatable(
                                "config.entityscalecore.category.depth"
                        )
                );

        depth.addEntry(
                entryBuilder.startBooleanToggle(
                                Component.translatable(
                                        "config.entityscalecore.depth.enabled"
                                ),
                                config.depthEnabled
                        )
                        .setDefaultValue(true)
                        .setSaveConsumer(
                                value -> config.depthEnabled = value
                        )
                        .build()
        );

        depth.addEntry(
                entryBuilder.startDoubleField(
                                Component.translatable(
                                        "config.entityscalecore.depth.bonus_per_block"
                                ),
                                config.depthBonusPerBlock
                        )
                        .setDefaultValue(0.01)
                        .setMin(0.0)
                        .setMax(1.0)
                        .setSaveConsumer(
                                value -> config.depthBonusPerBlock = value
                        )
                        .build()
        );

        depth.addEntry(
                entryBuilder.startDoubleField(
                                Component.translatable(
                                        "config.entityscalecore.depth.max_bonus"
                                ),
                                config.depthMaxBonus
                        )
                        .setDefaultValue(2.0)
                        .setMin(0.0)
                        .setMax(100.0)
                        .setSaveConsumer(
                                value -> config.depthMaxBonus = value
                        )
                        .build()
        );

        // =========================
        // 日数設定
        // =========================

        ConfigCategory day =
                builder.getOrCreateCategory(
                        Component.translatable(
                                "config.entityscalecore.category.day"
                        )
                );

        day.addEntry(
                entryBuilder.startDoubleField(
                                Component.translatable(
                                        "config.entityscalecore.day.bonus_per_day"
                                ),
                                config.dayBonusPerDay
                        )
                        .setDefaultValue(0.01)
                        .setMin(0.0)
                        .setMax(1.0)
                        .setSaveConsumer(
                                value -> config.dayBonusPerDay = value
                        )
                        .build()
        );

        day.addEntry(
                entryBuilder.startBooleanToggle(
                                Component.translatable(
                                        "config.entityscalecore.day.max_enabled"
                                ),
                                config.dayMaxEnabled
                        )
                        .setDefaultValue(false)
                        .setSaveConsumer(
                                value -> config.dayMaxEnabled = value
                        )
                        .build()
        );

        day.addEntry(
                entryBuilder.startDoubleField(
                                Component.translatable(
                                        "config.entityscalecore.day.max_bonus"
                                ),
                                config.dayMaxBonus
                        )
                        .setDefaultValue(10.0)
                        .setMin(0.0)
                        .setMax(100.0)
                        .setSaveConsumer(
                                value -> config.dayMaxBonus = value
                        )
                        .build()
        );

        // =========================
        // Elite設定
        // =========================

        ConfigCategory elite =
                builder.getOrCreateCategory(
                        Component.translatable(
                                "config.entityscalecore.category.elite"
                        )
                );

        elite.addEntry(
                entryBuilder.startBooleanToggle(
                                Component.translatable(
                                        "config.entityscalecore.elite.name_color_enabled"
                                ),
                                config.eliteNameColorEnabled
                        )
                        .setDefaultValue(true)
                        .setSaveConsumer(
                                value -> config.eliteNameColorEnabled = value
                        )
                        .build()
        );

        elite.addEntry(
                entryBuilder.startColorField(
                                Component.translatable(
                                        "config.entityscalecore.elite.name_color"
                                ),
                                config.eliteNameColor
                        )
                        .setDefaultValue(0xFFD700)
                        .setSaveConsumer(
                                value -> config.eliteNameColor = value
                        )
                        .build()
        );
        
        builder.setSavingRunnable(() -> {
            java.nio.file.Path configPath =
                    FMLPaths.CONFIGDIR.get()
                            .resolve("entityscale")
                            .resolve("entityscalecore.json");

            ScalingConfigManager.save(
                    configPath,
                    config
            );

            MobEliteScaling.setConfig(
                    config
            );
        });

        return builder.build();
    }
}