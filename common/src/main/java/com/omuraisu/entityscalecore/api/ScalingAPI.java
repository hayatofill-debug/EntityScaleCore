package com.omuraisu.entityscalecore.api;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public interface ScalingAPI {

    double getScale(
            MobScaleContext context
    );

    void registerModifier(
            ScaleModifier modifier
    );

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

    boolean removeArea(
            ResourceKey<Level> dimension,
            String id
    );

    List<AreaHandle> getAreas(
            ResourceKey<Level> dimension
    );

    MobScaleResult apply(
            MobScaleContext context
    );

    // ==========================================
    // Origin (基準座標) 関連 API
    // ==========================================

    /**
     * 指定したディメンションの Origin (基準座標) を取得します。
     *
     * @param dimension 対象ディメンション
     * @return Origin の座標。未登録の場合は null
     */
    @Nullable
    BlockPos getOrigin(
            ResourceKey<Level> dimension
    );

    /**
     * すべてのディメンションの Origin 一覧を取得します。
     *
     * @return ディメンションと BlockPos の読み取り専用マップ
     */
    Map<ResourceKey<Level>, BlockPos> getOrigins();
}