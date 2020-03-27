package me.kosinkadink.performantplants.util;

public class TimeHelper {

    public static int secondsToTicks(int seconds) {
        return seconds * 20;
    }

    public static int minutesToTicks(int minutes) {
        return secondsToTicks(minutes * 60);
    }

    public static long millisToTicks(long millis) {
        return (millis/1000) * 20;
    }

}
