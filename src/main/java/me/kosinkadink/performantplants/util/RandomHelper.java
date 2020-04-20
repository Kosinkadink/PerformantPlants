package me.kosinkadink.performantplants.util;

import java.util.concurrent.ThreadLocalRandom;

public class RandomHelper {

    public static boolean generateChancePercentage(double chance) {
        chance = Math.min(100, Math.max(0, chance));
        return ThreadLocalRandom.current().nextDouble() <= chance / 100.0;
    }

}
