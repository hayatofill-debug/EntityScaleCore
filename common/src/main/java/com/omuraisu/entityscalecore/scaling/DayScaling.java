package com.omuraisu.entityscalecore.scaling;

public class DayScaling {

    public static double calculateBonus(
            long days,
            double bonusPerDay
    ) {
        return days * bonusPerDay;
    }

}