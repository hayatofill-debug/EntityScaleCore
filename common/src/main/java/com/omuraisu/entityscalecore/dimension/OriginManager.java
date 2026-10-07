package com.omuraisu.entityscalecore.dimension;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@MethodsReturnNonnullByDefault
public class OriginManager extends SavedData {

    private static final String DATA_NAME = "entityscalecore_origins";

    private final Map<ResourceKey<Level>, BlockPos> origins = new HashMap<>();

    public OriginManager() {
    }

    /**
     * 保存データからOriginManagerを読み込みます。
     */
    public static OriginManager load(CompoundTag tag) {
        OriginManager manager = new OriginManager();

        ListTag originsTag = tag.getList("Origins", Tag.TAG_COMPOUND);

        for (int i = 0; i < originsTag.size(); i++) {
            CompoundTag originTag = originsTag.getCompound(i);

            String dimensionId = originTag.getString("Dimension");
            ResourceLocation location = ResourceLocation.tryParse(dimensionId);

            if (location == null) {
                continue;
            }

            ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, location);

            BlockPos origin = new BlockPos(
                    originTag.getInt("X"),
                    originTag.getInt("Y"),
                    originTag.getInt("Z")
            );

            manager.origins.put(dimension, origin);
        }

        return manager;
    }

    /**
     * OriginManagerをNBTに保存します。
     */
    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag originsTag = new ListTag();

        for (Map.Entry<ResourceKey<Level>, BlockPos> entry : origins.entrySet()) {
            CompoundTag originTag = new CompoundTag();

            originTag.putString("Dimension", entry.getKey().location().toString());

            BlockPos origin = entry.getValue();

            originTag.putInt("X", origin.getX());
            originTag.putInt("Y", origin.getY());
            originTag.putInt("Z", origin.getZ());

            originsTag.add(originTag);
        }

        tag.put("Origins", originsTag);

        return tag;
    }

    /**
     * サーバーからOriginManagerを取得します。
     * ワールド全体で共有するため、OverworldのSavedDataとして保存します。
     */
    public static OriginManager get(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);

        if (overworld == null) {
            throw new IllegalStateException("Overworld is not available.");
        }

        return overworld.getDataStorage().computeIfAbsent(
                OriginManager::load,
                OriginManager::new,
                DATA_NAME
        );
    }

    /**
     * DimensionのOriginを取得します。
     *
     * @param dimension 対象Dimension
     * @return Origin。未登録の場合はnull
     */
    @Nullable
    public BlockPos getOrigin(ResourceKey<Level> dimension) {
        return origins.get(dimension);
    }

    /**
     * DimensionのOriginを登録します。
     *
     * @param dimension 対象Dimension
     * @param origin Originの座標
     */
    @SuppressWarnings("unused")
    public void setOrigin(ResourceKey<Level> dimension, BlockPos origin) {
        origins.put(dimension, origin);
        setDirty();
    }

    /**
     * DimensionにOriginが登録されているか確認します。
     */
    public boolean hasOrigin(ResourceKey<Level> dimension) {
        return origins.containsKey(dimension);
    }

    /**
     * Originが存在しないDimensionだけ登録します。
     */
    public void registerIfAbsent(ResourceKey<Level> dimension, BlockPos origin) {
        if (origins.containsKey(dimension)) {
            return;
        }

        origins.put(dimension, origin);
        setDirty();

    }

    /**
     * すべてのOriginを取得します。
     */
    @SuppressWarnings("unused")
    public Map<ResourceKey<Level>, BlockPos> getOrigins() {
        return Map.copyOf(origins);
    }

    /**
     * DimensionのOriginを自動登録します。
     *
     * @param level 対象のServerLevel
     * @param firstArrivalPos その他のDimensionでの初回到達地点
     */
    public void registerInitialOrigin(ServerLevel level, BlockPos firstArrivalPos) {
        ResourceKey<Level> dimension = level.dimension();

        if (hasOrigin(dimension)) {
            return;
        }

        BlockPos origin;

        if (dimension.equals(Level.OVERWORLD)) {
            origin = level.getSharedSpawnPos();
        } else {
            origin = firstArrivalPos;
        }

        if (origin != null) {
            registerIfAbsent(dimension, origin);
        }
    }
}