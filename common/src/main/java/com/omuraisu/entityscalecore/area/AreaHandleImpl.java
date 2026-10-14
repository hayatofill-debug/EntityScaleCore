package com.omuraisu.entityscalecore.area;

import com.omuraisu.entityscalecore.api.AreaHandle;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class AreaHandleImpl implements AreaHandle {

    private final AreaManager areaManager;
    private final ResourceKey<Level> dimension;
    private final String id;

    private boolean valid = true;

    public AreaHandleImpl(
            AreaManager areaManager,
            ResourceKey<Level> dimension,
            String id
    ) {
        this.areaManager = areaManager;
        this.dimension = dimension;
        this.id = id;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void remove() {
        if (!valid) {
            return;
        }

        areaManager.removeArea(
                dimension,
                id
        );

        valid = false;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public boolean contains(int x, int y, int z) {
        if (!valid) {
            return false;
        }
        Area area = areaManager.getArea(dimension, id);
        return area != null && area.contains(x, y, z);
    }
}