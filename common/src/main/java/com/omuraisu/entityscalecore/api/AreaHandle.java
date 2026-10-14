package com.omuraisu.entityscalecore.api;

public interface AreaHandle {

    String id();

    void remove();

    boolean isValid();

    boolean contains(int x, int y, int z);
}