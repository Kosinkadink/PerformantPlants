package me.kosinkadink.performantplants.util;

import java.util.concurrent.ThreadLocalRandom;

public class RandomHelper {

    public static boolean generateChancePercentage(double chance) {
        chance = Math.min(100, Math.max(0, chance));
        return ThreadLocalRandom.current().nextDouble() <= chance / 100.0;
    }

    public static int generateRandomIntInRange(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max);
    }

    public static long generateRandomLongInRange(long min, long max) {
        return ThreadLocalRandom.current().nextLong(min, max);
    }

    public static double generateRandomDoubleInRange(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

}
