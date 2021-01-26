package me.kosinkadink.performantplants.util;

import java.util.concurrent.ThreadLocalRandom;

public class RandomHelper {

    public static boolean generateChancePercentage(double chance) {
        chance = Math.min(100, Math.max(0, chance));
        return ThreadLocalRandom.current().nextDouble() <= chance / 100.0;
    }

    public static int generateRandomIntInRange(int min, int max) {
        if (min == max) {
            return min;
        }
        else if (min > max) {
            return ThreadLocalRandom.current().nextInt(max, min+1);
        }
        return ThreadLocalRandom.current().nextInt(min, max+1);
    }

    public static long generateRandomLongInRange(long min, long max) {
        if (min == max) {
            return min;
        }
        else if (min > max) {
            return ThreadLocalRandom.current().nextLong(max, min+1);
        }
        return ThreadLocalRandom.current().nextLong(min, max+1);
    }

    public static double generateRandomDoubleInRange(double min, double max) {
        if (min == max) {
            return min;
        }
        else if (min > max) {
            return ThreadLocalRandom.current().nextDouble(max, min);
        }
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

}
