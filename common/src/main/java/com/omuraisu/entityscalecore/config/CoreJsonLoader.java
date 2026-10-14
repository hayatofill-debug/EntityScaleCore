package com.omuraisu.entityscalecore.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.omuraisu.entityscalecore.Constants;
import com.omuraisu.entityscalecore.core.CoreManager;
import com.omuraisu.entityscalecore.core.CoreType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class CoreJsonLoader {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void loadAndRegister(Path jsonPath, CoreManager coreManager, boolean enableLog) {
        if (!Files.exists(jsonPath)) {
            createDefaultJson(jsonPath);
        }

        try (Reader reader = Files.newBufferedReader(jsonPath)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root == null || !root.has("areas")) {
                return;
            }

            JsonArray areas = root.getAsJsonArray("areas");
            int loadedCount = 0;

            for (JsonElement element : areas) {
                if (!element.isJsonObject()) continue;
                JsonObject obj = element.getAsJsonObject();

                String id = obj.get("id").getAsString();
                String typeStr = obj.has("type") ? obj.get("type").getAsString() : "NORMAL";
                CoreType coreType = parseCoreType(typeStr);

                String dimStr = obj.has("dimension") ? obj.get("dimension").getAsString() : "minecraft:overworld";
                ResourceKey<Level> dimension = ResourceKey.create(
                        Registries.DIMENSION,
                        new ResourceLocation(dimStr)
                );

                int coreX = obj.has("core_x") ? obj.get("core_x").getAsInt() : 0;
                int coreY = obj.has("core_y") ? obj.get("core_y").getAsInt() : 0;
                int coreZ = obj.has("core_z") ? obj.get("core_z").getAsInt() : 0;
                BlockPos corePos = new BlockPos(coreX, coreY, coreZ);

                int minX = obj.get("min_x").getAsInt();
                int maxX = obj.get("max_x").getAsInt();
                int minY = obj.get("min_y").getAsInt();
                int maxY = obj.get("max_y").getAsInt();
                int minZ = obj.get("min_z").getAsInt();
                int maxZ = obj.get("max_z").getAsInt();

                double multiplier = obj.has("multiplier") ? obj.get("multiplier").getAsDouble() : 1.0;
                int priority = obj.has("priority") ? obj.get("priority").getAsInt() : 0;

                coreManager.registerCoreArea(
                        dimension, corePos, id, coreType,
                        minX, maxX, minZ, maxZ, minY, maxY,
                        multiplier, priority
                );

                if (enableLog) {
                    Constants.LOG.info("Registered core area: '{}' [Type: {}, Dimension: {}, Priority: {}]",
                            id, coreType, dimStr, priority);
                }
                loadedCount++;
            }

            if (enableLog) {
                Constants.LOG.info("Successfully loaded {} core area(s) from core_manager.json", loadedCount);
            }

        } catch (Exception e) {
            Constants.LOG.error("Failed to load core_manager.json", e);
        }
    }

    private static CoreType parseCoreType(String typeStr) {
        return CoreType.fromString(typeStr);
    }

    private static void createDefaultJson(Path jsonPath) {
        try {
            if (jsonPath.getParent() != null) {
                Files.createDirectories(jsonPath.getParent());
            }

            JsonObject root = new JsonObject();
            JsonArray areas = new JsonArray();

            JsonObject sampleArea = createSampleArea();

            areas.add(sampleArea);
            root.add("areas", areas);

            try (Writer writer = Files.newBufferedWriter(jsonPath)) {
                GSON.toJson(root, writer);
            }
        } catch (Exception e) {
            Constants.LOG.error("Failed to create default core_manager.json", e);
        }
    }

    private static JsonObject createSampleArea() {
        JsonObject sampleArea = new JsonObject();
        sampleArea.addProperty("id", "spawn_safe_zone");
        sampleArea.addProperty("type", "NORMAL");
        sampleArea.addProperty("dimension", "minecraft:overworld");
        sampleArea.addProperty("core_x", 0);
        sampleArea.addProperty("core_y", 64);
        sampleArea.addProperty("core_z", 0);
        sampleArea.addProperty("min_x", -100);
        sampleArea.addProperty("max_x", 100);
        sampleArea.addProperty("min_y", -64);
        sampleArea.addProperty("max_y", 320);
        sampleArea.addProperty("min_z", -100);
        sampleArea.addProperty("max_z", 100);
        sampleArea.addProperty("multiplier", 1.0);
        sampleArea.addProperty("priority", 100);
        return sampleArea;
    }
}