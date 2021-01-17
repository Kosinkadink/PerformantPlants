package me.kosinkadink.performantplants.util;

import me.kosinkadink.performantplants.PerformantPlants;

public class ServerHelper {

    public static boolean isPaperMC() {
        return PerformantPlants.getInstance().getServer().getName().equals("Paper");
    }

}
