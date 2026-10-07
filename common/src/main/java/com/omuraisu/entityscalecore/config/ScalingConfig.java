package com.omuraisu.entityscalecore.config;

public class ScalingConfig {

    public boolean distanceEnabled = true;

    public double distanceStart = 1000.0;
    public double distanceBlocksPerLevel = 100.0;
    public double distanceBonusPerLevel = 0.1;
    public double distanceMaxBonus = 10.0;
    public double distanceYWeight = 0.25;

    public boolean depthEnabled = true;

    public double depthBonusPerBlock = 0.01;
    public double depthMaxBonus = 2.0;

    public double dayBonusPerDay = 0.01;
    public boolean dayMaxEnabled = false;
    public double dayMaxBonus = 10.0;

    public boolean eliteNameColorEnabled = true;
    public int eliteNameColor = 0xFFD700;
}