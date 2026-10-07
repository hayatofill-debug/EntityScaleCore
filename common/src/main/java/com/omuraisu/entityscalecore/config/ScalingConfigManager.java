package com.omuraisu.entityscalecore.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.omuraisu.entityscalecore.Constants;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class ScalingConfigManager {

    private static final Gson GSON =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

    private ScalingConfigManager() {
    }

    public static ScalingConfig load(
            Path configPath
    ) {
        if (!Files.exists(configPath)) {
            ScalingConfig config =
                    new ScalingConfig();

            save(
                    configPath,
                    config
            );

            return config;
        }

        try (Reader reader =
                     Files.newBufferedReader(configPath)) {

            ScalingConfig config =
                    GSON.fromJson(
                            reader,
                            ScalingConfig.class
                    );

            if (config == null) {
                config = new ScalingConfig();
            }

            return config;

        } catch (IOException | RuntimeException e) {

            Constants.LOG.error(
                    "Failed to load EntityScaleCore config.",
                    e
            );

            return new ScalingConfig();
        }
    }

    public static void save(
            Path configPath,
            ScalingConfig config
    ) {
        try {

            Files.createDirectories(
                    configPath.getParent()
            );

            try (Writer writer =
                         Files.newBufferedWriter(configPath)) {

                GSON.toJson(
                        config,
                        writer
                );
            }

        } catch (IOException | RuntimeException e) {

            Constants.LOG.error(
                    "Failed to save EntityScaleCore config.",
                    e
            );
        }
    }
}